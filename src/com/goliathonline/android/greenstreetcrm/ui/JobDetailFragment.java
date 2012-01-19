package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Memos;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.SyncColumns;
import com.goliathonline.android.greenstreetcrm.util.FractionalTouchDelegate;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A fragment that shows detail information for a sandbox company, including company name,
 * description, product description, logo, etc.
 */
public class JobDetailFragment extends Fragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "JobDetailFragment";

    private Uri mJobUri;

    private ViewGroup mRootView;
    private TextView mName;
    private CompoundButton mStarred;
    private CompoundButton mCheck1;
    private CompoundButton mCheck2;
    private CompoundButton mCheck3;
    private CompoundButton mCheck4;
    private CompoundButton mCheck5;
    private CompoundButton mCheck6;
    private CompoundButton mCheck7;
    private CompoundButton mCheck8;
    private CompoundButton mCheck9;
    private CompoundButton mCheck10;

    private Spinner mStatus;
    private TextView mDesc;
    private TextView mLastChanged;
    private EditText mNewMemo;
    private Button mNewMemoButton;
    private ListView mMemoList;

    private Cursor mMemoCursor;
    private CursorAdapter mMemoAdapter;

    private String mNameString;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mJobUri = intent.getData();
        if (mJobUri== null) {
            return;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mJobUri == null) {
            return;
        }

        // Start background query to load job details
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.startQuery(JobsQuery._TOKEN, mJobUri, JobsQuery.PROJECTION);
        mHandler.startQuery(MemosQuery._TOKEN, null, Memos.buildMemoJobIdUri(Jobs.getJobId(mJobUri)),
                    MemosQuery.PROJECTION, null, null, CustomerContract.Memos.DEFAULT_SORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_job_detail, null);

        mName = (TextView) mRootView.findViewById(R.id.job_name);
        mStarred = (CompoundButton) mRootView.findViewById(R.id.star_button);

        mStarred.setFocusable(true);
        mStarred.setClickable(true);

        // Larger target triggers star toggle
        final View starParent = mRootView.findViewById(R.id.header_job);
        FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(0.6f, 0f, 1f, 0.8f));

        mCheck1 = (CompoundButton) mRootView.findViewById(R.id.checkBox1);
        mCheck2 = (CompoundButton) mRootView.findViewById(R.id.checkBox2);
        mCheck3 = (CompoundButton) mRootView.findViewById(R.id.checkBox3);
        mCheck4 = (CompoundButton) mRootView.findViewById(R.id.checkBox4);
        mCheck5 = (CompoundButton) mRootView.findViewById(R.id.checkBox5);
        mCheck6 = (CompoundButton) mRootView.findViewById(R.id.checkBox6);
        mCheck7 = (CompoundButton) mRootView.findViewById(R.id.checkBox7);
        mCheck8 = (CompoundButton) mRootView.findViewById(R.id.checkBox8);
        mCheck9 = (CompoundButton) mRootView.findViewById(R.id.checkBox9);
        mCheck10 = (CompoundButton) mRootView.findViewById(R.id.checkBox10);


        mStatus = (Spinner) mRootView.findViewById(R.id.job_status);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity().getBaseContext(), R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatus.setAdapter(adapter);
        mStatus.setOnItemSelectedListener(statusChanged);

        mDesc = (TextView) mRootView.findViewById(R.id.job_desc);
        mLastChanged = (TextView) mRootView.findViewById(R.id.lastEdit);

        mNewMemo = (EditText) mRootView.findViewById(R.id.newMemoEdit);
        mNewMemoButton = (Button) mRootView.findViewById(R.id.newMemoButton);
        mNewMemoButton.setOnClickListener(mOnAddMemoClick);
        mMemoList = (ListView) mRootView.findViewById(R.id.memoList);

        mMemoAdapter = new MemosAdapter(getActivity());
        mMemoList.setAdapter(mMemoAdapter);

        return mRootView;
    }

    /**
     * {@inheritDoc}
     */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == JobsQuery._TOKEN) {
            onJobQueryComplete(cursor);
        } else if (token == MemosQuery._TOKEN) {
            onMemoQueryComplete(cursor);
        } else {
            if (cursor != null)
                cursor.close();
        }
    }

    public void onJobQueryComplete(Cursor cursor)
    {
        try {
            if (!cursor.moveToFirst()) {
                return;
            }

            mNameString = cursor.getString(JobsQuery.JOB_ID);
            mName.setText(mNameString);

            // Unregister around setting checked state to avoid triggering
            // listener since change isn't user generated.
            mStarred.setOnCheckedChangeListener(null);
            mStarred.setChecked(cursor.getInt(JobsQuery.STARRED) != 0);
            mStarred.setOnCheckedChangeListener(this);

            mCheck1.setOnCheckedChangeListener(null);
            mCheck1.setChecked(cursor.getInt(JobsQuery.STEP1) != 0);
            mCheck1.setOnCheckedChangeListener(this);

            mCheck2.setOnCheckedChangeListener(null);
            mCheck2.setChecked(cursor.getInt(JobsQuery.STEP2) != 0);
            mCheck2.setOnCheckedChangeListener(this);

            mCheck3.setOnCheckedChangeListener(null);
            mCheck3.setChecked(cursor.getInt(JobsQuery.STEP3) != 0);
            mCheck3.setOnCheckedChangeListener(this);

            mCheck4.setOnCheckedChangeListener(null);
            mCheck4.setChecked(cursor.getInt(JobsQuery.STEP4) != 0);
            mCheck4.setOnCheckedChangeListener(this);

            mCheck5.setOnCheckedChangeListener(null);
            mCheck5.setChecked(cursor.getInt(JobsQuery.STEP5) != 0);
            mCheck5.setOnCheckedChangeListener(this);

            mCheck6.setOnCheckedChangeListener(null);
            mCheck6.setChecked(cursor.getInt(JobsQuery.STEP6) != 0);
            mCheck6.setOnCheckedChangeListener(this);

            mCheck7.setOnCheckedChangeListener(null);
            mCheck7.setChecked(cursor.getInt(JobsQuery.STEP7) != 0);
            mCheck7.setOnCheckedChangeListener(this);

            mCheck8.setOnCheckedChangeListener(null);
            mCheck8.setChecked(cursor.getInt(JobsQuery.STEP8) != 0);
            mCheck8.setOnCheckedChangeListener(this);

            mCheck9.setOnCheckedChangeListener(null);
            mCheck9.setChecked(cursor.getInt(JobsQuery.STEP9) != 0);
            mCheck9.setOnCheckedChangeListener(this);

            mCheck10.setOnCheckedChangeListener(null);
            mCheck10.setChecked(cursor.getInt(JobsQuery.STEP10) != 0);
            mCheck10.setOnCheckedChangeListener(this);

            mStatus.setSelection(cursor.getInt(JobsQuery.STATUS));
            mDesc.setText(cursor.getString(JobsQuery.DESC));
            mLastChanged.setText(UIUtils.formatTime(cursor.getLong(JobsQuery.UPDATED), getActivity().getBaseContext()));

        } finally {
            cursor.close();
        }
    }

    public void onMemoQueryComplete(Cursor cursor)
    {
        if (mMemoCursor != null) {
            // In case cancelOperation() doesn't work and we end up with consecutive calls to this
            // callback.
            getActivity().stopManagingCursor(mMemoCursor);
            mMemoCursor = null;
        }

        mMemoCursor = cursor;
        getActivity().startManagingCursor(mMemoCursor);
        mMemoAdapter.changeCursor(mMemoCursor);
        UIUtils.setListViewHeightBasedOnChildren(mMemoList);
    }

    private OnClickListener mOnAddMemoClick = new OnClickListener() {
        public void onClick(View v) {
            // Add new memo
            ContentValues values = new ContentValues();
            values.put(Memos.MEMO_JOB_ID, Jobs.getJobId(mJobUri));
            values.put(Memos.MEMO_TEXT, mNewMemo.getText().toString().trim());
            values.put(SyncColumns.UPDATED, UIUtils.getCurrentTime());
            getActivity().getContentResolver().insert(Memos.CONTENT_URI, values);
            mNewMemo.setText("");

            // Update job's timestamp
            values.clear();
            values.put(CustomerContract.SyncColumns.UPDATED, UIUtils.getCurrentTime());
    		mHandler.startUpdate(mJobUri, values);

            // Requery memos
            mHandler.startQuery(MemosQuery._TOKEN, null, Memos.buildMemoJobIdUri(Jobs.getJobId(mJobUri)),
                    MemosQuery.PROJECTION, null, null, CustomerContract.Memos.DEFAULT_SORT);
        }
    };

    /**
     * Handle toggling of starred checkbox.
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ContentValues values = new ContentValues();
        if (buttonView.equals(mStarred))
        	values.put(CustomerContract.Jobs.JOB_STARRED, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck1))
        	values.put(CustomerContract.Jobs.JOB_STEP1, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck2))
        	values.put(CustomerContract.Jobs.JOB_STEP2, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck3))
        	values.put(CustomerContract.Jobs.JOB_STEP3, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck4))
        	values.put(CustomerContract.Jobs.JOB_STEP4, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck5))
        	values.put(CustomerContract.Jobs.JOB_STEP5, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck6))
        	values.put(CustomerContract.Jobs.JOB_STEP6, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck7))
        	values.put(CustomerContract.Jobs.JOB_STEP7, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck8))
        	values.put(CustomerContract.Jobs.JOB_STEP8, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck9))
        	values.put(CustomerContract.Jobs.JOB_STEP9, isChecked ? 1 : 0);
        else if (buttonView.equals(mCheck10))
        	values.put(CustomerContract.Jobs.JOB_STEP10, isChecked ? 1 : 0);
        if (!buttonView.equals(mStarred))
        	values.put(CustomerContract.SyncColumns.UPDATED, UIUtils.getCurrentTime());
        mHandler.startUpdate(mJobUri, values);
    }

    private OnItemSelectedListener statusChanged = new OnItemSelectedListener() {
    	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    	{
    		int index = mStatus.getSelectedItemPosition();
    		final ContentValues values = new ContentValues();
    		values.put(CustomerContract.Jobs.JOB_STATUS, index);
    		values.put(CustomerContract.SyncColumns.UPDATED, UIUtils.getCurrentTime());
    		mHandler.startUpdate(mJobUri, values);
    	}

    	public void onNothingSelected(AdapterView<?> arg0) {}
    };

    private class MemosAdapter extends CursorAdapter {
        public MemosAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_memo,
                    parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.memo_text)).setText(
                    cursor.getString(MemosQuery.MEMO_TEXT));

            ((TextView) view.findViewById(R.id.memo_timestamp)).setText(
                    UIUtils.formatTime(cursor.getLong(MemosQuery.UPDATED), context));
        }
    }

    /**
     * {@link com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs} query parameters.
     */
    private interface JobsQuery {
        int _TOKEN = 0x1;

        String[] PROJECTION = {
                BaseColumns._ID,
                CustomerContract.Jobs.JOB_ID,
                CustomerContract.Jobs.JOB_DESC,
                CustomerContract.Jobs.JOB_STATUS,
                CustomerContract.Jobs.JOB_CUST_ID,
                CustomerContract.Jobs.JOB_STEP1,
                CustomerContract.Jobs.JOB_STEP2,
                CustomerContract.Jobs.JOB_STEP3,
                CustomerContract.Jobs.JOB_STEP4,
                CustomerContract.Jobs.JOB_STEP5,
                CustomerContract.Jobs.JOB_STEP6,
                CustomerContract.Jobs.JOB_STEP7,
                CustomerContract.Jobs.JOB_STEP8,
                CustomerContract.Jobs.JOB_STEP9,
                CustomerContract.Jobs.JOB_STEP10,
                CustomerContract.Jobs.JOB_STARRED,
                SyncColumns.UPDATED,
        };

        int _ID = 0;
        int JOB_ID = 1;
        int DESC = 2;
        int STATUS = 3;
        int CUST_ID = 4;
        int STEP1 = 5;
        int STEP2 = 6;
        int STEP3 = 7;
        int STEP4 = 8;
        int STEP5 = 9;
        int STEP6 = 10;
        int STEP7 = 11;
        int STEP8 = 12;
        int STEP9 = 13;
        int STEP10 = 14;
        int STARRED = 15;
        int UPDATED = 16;
    }

    private interface MemosQuery {
        int _TOKEN = 0x2;

        String[] PROJECTION = {
            BaseColumns._ID,
            CustomerContract.Memos.MEMO_TEXT,
            SyncColumns.UPDATED,
        };

        int _ID = 0;
        int MEMO_TEXT = 1;
        int UPDATED = 2;
    }
}

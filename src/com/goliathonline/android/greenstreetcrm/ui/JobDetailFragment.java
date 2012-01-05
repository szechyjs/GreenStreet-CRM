package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.util.FractionalTouchDelegate;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

    private TextView mStatus;
    private TextView mDesc;
    
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
        mHandler.startQuery(mJobUri, JobsQuery.PROJECTION);
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

        mStatus = (TextView) mRootView.findViewById(R.id.job_status);
        mDesc = (TextView) mRootView.findViewById(R.id.job_desc);

        return mRootView;
    }

    /**
     * Build a {@link android.view.View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     *
     * @return View
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getActivity().getLayoutInflater()
                .inflate(R.layout.tab_indicator,
                        (ViewGroup) mRootView.findViewById(android.R.id.tabs), false);
        indicator.setText(textRes);
        return indicator;
    }

    /**
     * {@inheritDoc}
     */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

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

            mStatus.setText(cursor.getString(JobsQuery.STATUS));
            mDesc.setText(cursor.getString(JobsQuery.DESC));

        } finally {
            cursor.close();
        }
    }

    /**
     * Handle toggling of starred checkbox.
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ContentValues values = new ContentValues();
        values.put(CustomerContract.Jobs.JOB_STARRED, isChecked ? 1 : 0);
        mHandler.startUpdate(mJobUri, values);
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
                CustomerContract.Jobs.JOB_STARRED,
        };

        int _ID = 0;
        int JOB_ID = 1;
        int DESC = 2;
        int STATUS = 3;
        int CUST_ID = 4;
        int STARRED = 5;
    }
}
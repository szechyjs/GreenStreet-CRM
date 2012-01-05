package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.SyncColumns;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class JobEditFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {
	private static final String TAG = "JobEditFragment";
	
	private Uri mJobUri;
	
	private ViewGroup mRootView;
	
	private EditText mJobId;
	private Spinner mCustomerId;
	private Cursor mCustCur;
	private SimpleCursorAdapter mCustCurAdapt;
	
	private NotifyingAsyncQueryHandler mHandler;
	
	private String mNameString;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mJobUri = intent.getData();
        if (mJobUri== null) {
            // Do something for new customer here.
        	return;
        }
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mJobUri == null) {
        	// New Customer
            return;
        }

        // Start background query to load vendor details
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.startQuery(mJobUri, JobsQuery.PROJECTION);
//        mHandler.startQuery(CustomersQuery._TOKEN, null, CustomerContract.Customers.CONTENT_URI, CustomersQuery.PROJECTION, null, null,
//                CustomerContract.Customers.DEFAULT_SORT);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_job_edit, null);
    	mJobId = (EditText) mRootView.findViewById(R.id.job_id);
    	mCustomerId = (Spinner) mRootView.findViewById(R.id.customer_id);
    	
    	mCustCur = getActivity().managedQuery(CustomerContract.Customers.CONTENT_URI, CustomersQuery.PROJECTION, null, null, null);
    	mCustCurAdapt = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
    			mCustCur,
    			new String[] {CustomerContract.Customers.CUSTOMER_LASTNAME},
    			new int[] {android.R.id.text1});
    	mCustCurAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mCustomerId.setAdapter(mCustCurAdapt);

        return mRootView;
    }
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu_items, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_save:
            	
            	ContentValues values = new ContentValues();
                values.put(Jobs.JOB_ID, mJobId.getText().toString());
                values.put(Jobs.JOB_CUST_ID, mCustomerId.getSelectedItemId());
                values.put(SyncColumns.UPDATED, UIUtils.getCurrentTime());

                if (mJobUri == null)
                	mJobUri = getActivity().getContentResolver().insert(Jobs.CONTENT_URI, values);
                else
                	getActivity().getContentResolver().update(mJobUri, values, null, null);
            	
                JobDetailFragment fg = new JobDetailFragment();
                Bundle args = new Bundle();
                args.putParcelable("_uri", mJobUri);
                fg.setArguments(args);
                
                if (UIUtils.isHoneycombTablet(getActivity()))
                {
                	final FragmentTransaction ft = getFragmentManager().beginTransaction();
            		ft.replace(R.id.fragment_container_job_detail, fg);
            		ft.commit();
                }
            	else
            	{
            		final Intent intent = new Intent(Intent.ACTION_VIEW, mJobUri);
                    startActivity(intent);
                    getActivity().finish();
            	}
                return true;
        }
        return super.onOptionsItemSelected(item);
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

            mJobId.setText(cursor.getString(JobsQuery.JOB_ID));

        } finally {
            cursor.close();
        }
		
	}
	
	/**
     * {@link com.goliathonline.android.greenstreetcrm.provider.ScheduleContract.Customers} query parameters.
     */
    private interface JobsQuery {
    	int _TOKEN = 0x1;
        String[] PROJECTION = {
                CustomerContract.Jobs.JOB_ID,
                CustomerContract.Jobs.JOB_CUST_ID,
                CustomerContract.Jobs.JOB_STARRED,
        };

        int JOB_ID = 0;
        int CUST_ID = 1;
        int STARRED = 2;
    }
    
	/**
     * {@link com.goliathonline.android.greenstreetcrm.provider.ScheduleContract.Customers} query parameters.
     */
    private interface CustomersQuery {
    	int _TOKEN = 0x2;
        String[] PROJECTION = {
                CustomerContract.Customers._ID,
                CustomerContract.Customers.CUSTOMER_LASTNAME,
                CustomerContract.Customers.CUSTOMER_FIRSTNAME,
                CustomerContract.Customers.CUSTOMER_COMPANY,
        };

        int CUST_ID = 0;
        int LASTNAME = 1;
        int FIRSTNAME = 2;
        int COMPANY = 3;
    }
	
	
}

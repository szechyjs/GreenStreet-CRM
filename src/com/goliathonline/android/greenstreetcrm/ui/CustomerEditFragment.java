package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CustomerEditFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {
	private static final String TAG = "CustomerEditFragment";
	
	private Uri mCustomerUri;
	
	private ViewGroup mRootView;
	
	private NotifyingAsyncQueryHandler mHandler;
	
	private String mNameString;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mCustomerUri = intent.getData();
        if (mCustomerUri== null) {
            // Do something for new customer here.
        	return;
        }

        setHasOptionsMenu(true);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCustomerUri == null) {
        	// New Customer
            return;
        }

        // Start background query to load vendor details
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.startQuery(mCustomerUri, CustomersQuery.PROJECTION);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_customer_edit, null);

        return mRootView;
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

            mNameString = cursor.getString(CustomersQuery.LASTNAME);

        } finally {
            cursor.close();
        }
		
	}
	
	/**
     * {@link com.goliathonline.android.greenstreetcrm.provider.ScheduleContract.Customers} query parameters.
     */
    private interface CustomersQuery {
        String[] PROJECTION = {
                CustomerContract.Customers.CUSTOMER_LASTNAME,
                CustomerContract.Customers.CUSTOMER_FIRSTNAME,
                CustomerContract.Customers.CUSTOMER_COMPANY,
                CustomerContract.Customers.CUSTOMER_ADDRESS,
                CustomerContract.Customers.CUSTOMER_CITY,
                CustomerContract.Customers.CUSTOMER_STATE,
                CustomerContract.Customers.CUSTOMER_ZIPCODE,
                CustomerContract.Customers.CUSTOMER_PHONE,
                CustomerContract.Customers.CUSTOMER_MOBILE,
                CustomerContract.Customers.CUSTOMER_EMAIL,
                CustomerContract.Customers.CUSTOMER_STARRED,
        };

        int LASTNAME = 0;
        int FIRSTNAME = 1;
        int COMPANY = 2;
        int ADDRESS = 3;
        int CITY = 4;
        int STATE = 5;
        int ZIPCODE = 6;
        int PHONE = 7;
        int MOBILE = 8;
        int EMAIL = 9;
    }
	
	
}

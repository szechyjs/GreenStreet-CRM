package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
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

public class CustomerEditFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {
	private static final String TAG = "CustomerEditFragment";
	
	private Uri mCustomerUri;
	
	private ViewGroup mRootView;
	
	private EditText mLastName;
	private EditText mFirstName;
	private EditText mCompany;
	private EditText mAddress;
	private EditText mCity;
	private EditText mState;
	private EditText mZipcode;
	private EditText mPhone;
	private EditText mMobile;
	private EditText mEmail;
	
	private NotifyingAsyncQueryHandler mHandler;
	
	private String mNameString;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mCustomerUri = intent.getData();
        if (mCustomerUri== null) {
            // Do something for new customer here.
        	return;
        }
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
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu_items, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_save:
            	
            	mFirstName = (EditText) mRootView.findViewById(R.id.firstName);
            	mLastName = (EditText) mRootView.findViewById(R.id.lastName);
            	mCompany = (EditText) mRootView.findViewById(R.id.company);
            	mAddress = (EditText) mRootView.findViewById(R.id.address);
            	mCity = (EditText) mRootView.findViewById(R.id.city);
            	mState = (EditText) mRootView.findViewById(R.id.state);
            	mZipcode = (EditText) mRootView.findViewById(R.id.zipcode);
            	mPhone = (EditText) mRootView.findViewById(R.id.phone);
            	mMobile = (EditText) mRootView.findViewById(R.id.mobile);
            	mEmail = (EditText) mRootView.findViewById(R.id.email);
            	       	
            	ContentValues values = new ContentValues();
                values.put(Customers.CUSTOMER_LASTNAME, mLastName.getText().toString());
                values.put(Customers.CUSTOMER_FIRSTNAME, mFirstName.getText().toString());
                values.put(Customers.CUSTOMER_COMPANY, mCompany.getText().toString());
                values.put(Customers.CUSTOMER_ADDRESS, mAddress.getText().toString());
                values.put(Customers.CUSTOMER_CITY, mCity.getText().toString());
                values.put(Customers.CUSTOMER_ZIPCODE, mZipcode.getText().toString());
                values.put(Customers.CUSTOMER_STATE, mState.getText().toString());
                values.put(Customers.CUSTOMER_PHONE, mPhone.getText().toString());
                values.put(Customers.CUSTOMER_MOBILE, mMobile.getText().toString());
                values.put(Customers.CUSTOMER_EMAIL, mEmail.getText().toString());
                values.put(SyncColumns.UPDATED, "test");
                Uri uri = getActivity().getContentResolver().insert(Customers.CONTENT_URI, values);
            	
                CustomerDetailFragment fg = new CustomerDetailFragment();
                Bundle args = new Bundle();
                args.putParcelable("_uri", uri);
                fg.setArguments(args);
                
                if (UIUtils.isHoneycombTablet(getActivity()))
                {
                	final FragmentTransaction ft = getFragmentManager().beginTransaction();
            		ft.replace(R.id.fragment_container_customer_detail, fg);
            		ft.commit();
                }
            	else
            	{
            		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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

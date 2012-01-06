package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.SyncColumns;
import com.goliathonline.android.greenstreetcrm.util.FractionalTouchDelegate;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * A fragment that shows detail information for a sandbox company, including company name,
 * description, product description, logo, etc.
 */
public class CustomerDetailFragment extends Fragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "CustomerDetailFragment";

    private Uri mCustomerUri;

    private ViewGroup mRootView;
    private TextView mName;
    private CompoundButton mStarred;

    private TextView mCompany;
    private TextView mAddress;
    private TextView mPhone;
    private TextView mMobile;
    private TextView mEmail;
    private TextView mLastChanged;
    
    private String mNameString;
    private String mAddress1;
    private String mAddress2;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mCustomerUri = intent.getData();
        if (mCustomerUri== null) {
            return;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCustomerUri == null) {
            return;
        }

        // Start background query to load customer details
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.startQuery(mCustomerUri, CustomersQuery.PROJECTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_customer_detail, null);

        mName = (TextView) mRootView.findViewById(R.id.customer_name);
        mStarred = (CompoundButton) mRootView.findViewById(R.id.star_button);

        mStarred.setFocusable(true);
        mStarred.setClickable(true);

        // Larger target triggers star toggle
        final View starParent = mRootView.findViewById(R.id.header_customer);
        FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(0.6f, 0f, 1f, 0.8f));

        mCompany = (TextView) mRootView.findViewById(R.id.customer_company);
        mAddress = (TextView) mRootView.findViewById(R.id.customer_address);
        mPhone = (TextView) mRootView.findViewById(R.id.customer_phone);
        mMobile = (TextView) mRootView.findViewById(R.id.customer_mobile);
        mEmail = (TextView) mRootView.findViewById(R.id.customer_email);
        mLastChanged = (TextView) mRootView.findViewById(R.id.lastEdit);

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

            mNameString = cursor.getString(CustomersQuery.LASTNAME) + ", " + cursor.getString(CustomersQuery.FIRSTNAME);
            mName.setText(mNameString);

            // Unregister around setting checked state to avoid triggering
            // listener since change isn't user generated.
            mStarred.setOnCheckedChangeListener(null);
            mStarred.setChecked(cursor.getInt(CustomersQuery.STARRED) != 0);
            mStarred.setOnCheckedChangeListener(this);

            mCompany.setText(cursor.getString(CustomersQuery.COMPANY));
            mAddress1 = cursor.getString(CustomersQuery.ADDRESS);
            mAddress2 = cursor.getString(CustomersQuery.CITY) + ", " + cursor.getString(CustomersQuery.STATE) + "  " + cursor.getString(CustomersQuery.ZIP);
            mAddress.setText(mAddress1 + "\n" + mAddress2);
            mAddress.setOnClickListener(mAddressClick);
            mPhone.setText("Phone: " + cursor.getString(CustomersQuery.PHONE));
            mPhone.setOnClickListener(mPhoneClick);
            mMobile.setText("Mobile: " + cursor.getString(CustomersQuery.MOBILE));
            mMobile.setOnClickListener(mMobileClick);
            mEmail.setText(cursor.getString(CustomersQuery.EMAIL));
            mEmail.setOnClickListener(mEmailClick);
            mLastChanged.setText(UIUtils.formatTime(cursor.getLong(CustomersQuery.UPDATED), getActivity().getBaseContext()));
            
            

        } finally {
            cursor.close();
        }
    }

    /**
     * Handle toggling of starred checkbox.
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ContentValues values = new ContentValues();
        values.put(CustomerContract.Customers.CUSTOMER_STARRED, isChecked ? 1 : 0);
        mHandler.startUpdate(mCustomerUri, values);
    }
    
    private OnClickListener mPhoneClick = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mPhone.getText().subSequence(7, mPhone.getText().length())));
    		startActivity(intent);
        }
    };
    
    private OnClickListener mMobileClick = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mMobile.getText().subSequence(8, mMobile.getText().length())));
    		startActivity(intent);
        }
    };
    
    private OnClickListener mEmailClick = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(Intent.ACTION_SEND);
        	intent.setType("plain/text");
        	String[] to = { mEmail.getText().toString() };
        	intent.putExtra(Intent.EXTRA_EMAIL, to);
    		startActivity(Intent.createChooser(intent, "Send email using:"));
        }
    };
    
    private OnClickListener mAddressClick = new OnClickListener() {
        public void onClick(View v) {
        	Uri mapUri = Uri.parse("geo:0,0?q=" + mAddress1 + ", " + mAddress2);
        	Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
    		startActivity(intent);
        }
    };

    /**
     * {@link com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers} query parameters.
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
                SyncColumns.UPDATED,
        };

        int LASTNAME = 0;
        int FIRSTNAME = 1;
        int COMPANY = 2;
        int ADDRESS = 3;
        int CITY = 4;
        int STATE = 5;
        int ZIP = 6;
        int PHONE = 7;
        int MOBILE = 8;
        int EMAIL = 9;
        int STARRED = 10;
        int UPDATED = 11;
    }
}

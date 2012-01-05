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

    private TextView mUrl;
    private TextView mDesc;
    private TextView mProductDesc;
    private TextView mLastChanged;
    
    private String mNameString;

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

        mUrl = (TextView) mRootView.findViewById(R.id.customer_url);
        mDesc = (TextView) mRootView.findViewById(R.id.customer_desc);
        mProductDesc = (TextView) mRootView.findViewById(R.id.customer_product_desc);
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

            mUrl.setText(cursor.getString(CustomersQuery.EMAIL));
            mDesc.setText(cursor.getString(CustomersQuery.ADDRESS));
            mProductDesc.setText(cursor.getString(CustomersQuery.CITY));
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

    /**
     * {@link com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers} query parameters.
     */
    private interface CustomersQuery {
        String[] PROJECTION = {
                CustomerContract.Customers.CUSTOMER_LASTNAME,
                CustomerContract.Customers.CUSTOMER_FIRSTNAME,
                CustomerContract.Customers.CUSTOMER_CITY,
                CustomerContract.Customers.CUSTOMER_ADDRESS,
                CustomerContract.Customers.CUSTOMER_EMAIL,
                CustomerContract.Customers.CUSTOMER_PHONE,
                CustomerContract.Customers.CUSTOMER_MOBILE,
                CustomerContract.Customers.CUSTOMER_STARRED,
                SyncColumns.UPDATED,
        };

        int LASTNAME = 0;
        int FIRSTNAME = 1;
        int CITY = 2;
        int ADDRESS = 3;
        int EMAIL = 4;
        int PHONE = 5;
        int MOBILE = 6;
        int STARRED = 7;
        int UPDATED = 8;
    }
}

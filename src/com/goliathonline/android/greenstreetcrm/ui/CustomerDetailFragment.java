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

    private Uri mVendorUri;

    private ViewGroup mRootView;
    private TextView mName;
    private CompoundButton mStarred;

    private TextView mUrl;
    private TextView mDesc;
    private TextView mProductDesc;
    
    private String mNameString;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mVendorUri = intent.getData();
        if (mVendorUri== null) {
            return;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mVendorUri == null) {
            return;
        }

        // Start background query to load vendor details
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.startQuery(mVendorUri, VendorsQuery.PROJECTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_customer_detail, null);

        mName = (TextView) mRootView.findViewById(R.id.vendor_name);
        mStarred = (CompoundButton) mRootView.findViewById(R.id.star_button);

        mStarred.setFocusable(true);
        mStarred.setClickable(true);

        // Larger target triggers star toggle
        final View starParent = mRootView.findViewById(R.id.header_vendor);
        FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(0.6f, 0f, 1f, 0.8f));

        mUrl = (TextView) mRootView.findViewById(R.id.vendor_url);
        mDesc = (TextView) mRootView.findViewById(R.id.vendor_desc);
        mProductDesc = (TextView) mRootView.findViewById(R.id.vendor_product_desc);

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

            mNameString = cursor.getString(VendorsQuery.LASTNAME) + ", " + cursor.getString(VendorsQuery.FIRSTNAME);
            mName.setText(mNameString);

            // Unregister around setting checked state to avoid triggering
            // listener since change isn't user generated.
            mStarred.setOnCheckedChangeListener(null);
            mStarred.setChecked(cursor.getInt(VendorsQuery.STARRED) != 0);
            mStarred.setOnCheckedChangeListener(this);

            mUrl.setText(cursor.getString(VendorsQuery.EMAIL));
            mDesc.setText(cursor.getString(VendorsQuery.ADDRESS));
            mProductDesc.setText(cursor.getString(VendorsQuery.CITY));

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
        mHandler.startUpdate(mVendorUri, values);
    }

    /**
     * {@link com.google.android.apps.iosched.provider.ScheduleContract.Vendors} query parameters.
     */
    private interface VendorsQuery {
        String[] PROJECTION = {
                CustomerContract.Customers.CUSTOMER_LASTNAME,
                CustomerContract.Customers.CUSTOMER_FIRSTNAME,
                CustomerContract.Customers.CUSTOMER_CITY,
                CustomerContract.Customers.CUSTOMER_ADDRESS,
                CustomerContract.Customers.CUSTOMER_EMAIL,
                CustomerContract.Customers.CUSTOMER_PHONE,
                CustomerContract.Customers.CUSTOMER_MOBILE,
                CustomerContract.Customers.CUSTOMER_STARRED,
        };

        int LASTNAME = 0;
        int FIRSTNAME = 1;
        int CITY = 2;
        int ADDRESS = 3;
        int EMAIL = 4;
        int PHONE = 5;
        int MOBILE = 6;
        int STARRED = 7;
    }
}

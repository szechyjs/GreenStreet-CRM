package com.goliathonline.android.greenstreetcrm.ui.tablet;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.ui.BaseActivity;
import com.goliathonline.android.greenstreetcrm.ui.BaseMultiPaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.CustomerDetailFragment;
import com.goliathonline.android.greenstreetcrm.ui.CustomersFragment;
import com.goliathonline.android.greenstreetcrm.ui.phone.CustomerDetailActivity;
import com.goliathonline.android.greenstreetcrm.ui.phone.CustomersActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 * A multi-pane activity, consisting of a {@link TracksDropdownFragment}, a
 * {@link CustomersFragment}, and {@link CustomerDetailFragment}. This activity is very similar in
 * function to {@link SessionsMultiPaneActivity}.
 *
 * This activity requires API level 11 or greater because {@link TracksDropdownFragment} requires
 * API level 11.
 */
public class CustomersMultiPaneActivity extends BaseMultiPaneActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_customers);
        
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(CustomerContract.Customers.CONTENT_URI);
        openActivityOrFragment(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup)
                findViewById(R.id.fragment_container_customer_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 0) {
            findViewById(R.id.fragment_container_customer_detail).setBackgroundColor(0xffffffff);
        }
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (CustomersActivity.class.getName().equals(activityClassName)) {
            return new FragmentReplaceInfo(
                    CustomersFragment.class,
                    "customers",
                    R.id.fragment_container_customers);
        } else if (CustomerDetailActivity.class.getName().equals(activityClassName)) {
            findViewById(R.id.fragment_container_customer_detail).setBackgroundColor(
                    0xffffffff);
            return new FragmentReplaceInfo(
                    CustomerDetailFragment.class,
                    "customer_detail",
                    R.id.fragment_container_customer_detail);
        }
        return null;
    }
}

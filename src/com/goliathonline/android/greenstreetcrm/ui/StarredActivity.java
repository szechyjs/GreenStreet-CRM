package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
import com.goliathonline.android.greenstreetcrm.ui.phone.CustomerDetailActivity;
import com.goliathonline.android.greenstreetcrm.ui.phone.JobDetailActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * An activity that shows the user's starred jobs and customers. This activity can be
 * either single or multi-pane, depending on the device configuration. We want the multi-pane
 * support that {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class StarredActivity extends BaseMultiPaneActivity {

    public static final String TAG_JOBS = "jobs";
    public static final String TAG_CUSTOMERS = "customers";

    private TabHost mTabHost;
    private TabWidget mTabWidget;

    private JobsFragment mJobsFragment;
    private CustomersFragment mCustomersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred);
        getActivityHelper().setupActionBar(getTitle(), 0);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        mTabHost.setup();

        setupJobsTab();
        setupCustomersTab();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_starred_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 1) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
    }

    /**
     * Build and add "jobs" tab.
     */
    private void setupJobsTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_jobs);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, Jobs.CONTENT_STARRED_URI);

        final FragmentManager fm = getSupportFragmentManager();
        mJobsFragment = (JobsFragment) fm.findFragmentByTag("jobs");
        if (mJobsFragment == null) {
        	mJobsFragment = new JobsFragment();
        	mJobsFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_jobs, mJobsFragment, "jobs")
                    .commit();
        }

        // Jobs content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_JOBS)
                .setIndicator(buildIndicator(R.string.starred_jobs))
                .setContent(R.id.fragment_jobs));
    }

    /**
     * Build and add "customers" tab.
     */
    private void setupCustomersTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_customers);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, Customers.CONTENT_STARRED_URI);

        final FragmentManager fm = getSupportFragmentManager();

        mCustomersFragment = (CustomersFragment) fm.findFragmentByTag("customers");
        if (mCustomersFragment == null) {
        	mCustomersFragment = new CustomersFragment();
        	mCustomersFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_customers, mCustomersFragment, "customers")
                    .commit();
        }

        // Customers content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_CUSTOMERS)
                .setIndicator(buildIndicator(R.string.starred_customers))
                .setContent(R.id.fragment_customers));
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                mTabWidget, false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (findViewById(R.id.fragment_container_starred_detail) != null) {
            // The layout we currently have has a detail container, we can add fragments there.
            findViewById(android.R.id.empty).setVisibility(View.GONE);
            if (JobDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        JobDetailFragment.class,
                        "job_detail",
                        R.id.fragment_container_starred_detail);
            } else if (CustomerDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        CustomerDetailFragment.class,
                        "customer_detail",
                        R.id.fragment_container_starred_detail);
            }
        }
        return null;
    }

    private void clearSelectedItems() {
        if (mJobsFragment != null) {
        	mJobsFragment.clearCheckedPosition();
        }
        if (mCustomersFragment != null) {
        	mCustomersFragment.clearCheckedPosition();
        }
    }
}

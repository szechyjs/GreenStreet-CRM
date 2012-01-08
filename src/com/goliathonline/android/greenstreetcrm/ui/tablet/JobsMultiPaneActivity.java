package com.goliathonline.android.greenstreetcrm.ui.tablet;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.ui.BaseMultiPaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.JobDetailFragment;
import com.goliathonline.android.greenstreetcrm.ui.JobsFragment;
import com.goliathonline.android.greenstreetcrm.ui.phone.JobDetailActivity;
import com.goliathonline.android.greenstreetcrm.ui.phone.JobsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

/**
 * A multi-pane activity, consisting of a {@link TracksDropdownFragment}, a
 * {@link JobsFragment}, and {@link JobDetailFragment}. This activity is very similar in
 * function to {@link SessionsMultiPaneActivity}.
 *
 * This activity requires API level 11 or greater because {@link TracksDropdownFragment} requires
 * API level 11.
 */
public class JobsMultiPaneActivity extends BaseMultiPaneActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_jobs);
        
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(CustomerContract.Jobs.CONTENT_URI);
        openActivityOrFragment(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup)
                findViewById(R.id.fragment_container_job_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 0) {
            findViewById(R.id.fragment_container_job_detail).setBackgroundColor(0xffffffff);
        }
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (JobsActivity.class.getName().equals(activityClassName)) {
            return new FragmentReplaceInfo(
                    JobsFragment.class,
                    "jobs",
                    R.id.fragment_container_jobs);
        } else if (JobDetailActivity.class.getName().equals(activityClassName)) {
            findViewById(R.id.fragment_container_job_detail).setBackgroundColor(
                    0xffffffff);
            return new FragmentReplaceInfo(
                    JobDetailFragment.class,
                    "job_detail",
                    R.id.fragment_container_job_detail);
        }
        return null;
    }
}

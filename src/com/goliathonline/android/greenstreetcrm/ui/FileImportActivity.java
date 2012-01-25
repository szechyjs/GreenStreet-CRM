package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public class FileImportActivity extends BaseSinglePaneActivity {
	public static final String TAG = "file import";
	
	@Override
    protected Fragment onCreatePane() {
		JobsFragment jobsFragment = new JobsFragment();
		final Intent intent = new Intent(Intent.ACTION_VIEW, Jobs.CONTENT_URI);
		jobsFragment.setArguments(intentToFragmentArguments(intent));
        return jobsFragment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }    


}

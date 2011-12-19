package com.goliathonline.android.greenstreetcrm.ui.phone;

import com.goliathonline.android.greenstreetcrm.ui.BaseSinglePaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.CustomerDetailFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class CustomerDetailActivity extends BaseSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new CustomerDetailFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }
}

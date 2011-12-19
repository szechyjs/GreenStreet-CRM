package com.goliathonline.android.greenstreetcrm.ui.phone;

import com.goliathonline.android.greenstreetcrm.ui.BaseSinglePaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.CustomersFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class CustomersActivity extends BaseSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new CustomersFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }
}

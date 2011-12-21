package com.goliathonline.android.greenstreetcrm.ui.phone;

import com.goliathonline.android.greenstreetcrm.ui.BaseSinglePaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.CustomerEditFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class CustomerEditActivity extends BaseSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new CustomerEditFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }
}

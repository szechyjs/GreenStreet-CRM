package com.goliathonline.android.greenstreetcrm.service;

import com.goliathonline.android.greenstreetcrm.R;
import com.pushlink.android.PushLink;

import android.app.Application;
import android.util.Log;

public class PushLinkSetup extends Application {
	private static final String TAG = "PushLinkSetup";
	
	@Override
	public void onCreate() {
		super.onCreate();
		PushLink.start(this, R.drawable.ic_launcher, 10, "63f9131513fa3991", true);
		Log.d(TAG, "PushLink Started.");
	}
}

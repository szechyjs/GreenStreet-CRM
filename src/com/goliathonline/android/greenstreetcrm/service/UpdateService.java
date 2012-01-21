package com.goliathonline.android.greenstreetcrm.service;

import com.goliathonline.android.greenstreetcrm.R;
import com.pushlink.android.PushLink;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String TAG = "UpdateService";
	private PushLink mPushLink;
	
	public UpdateService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mPushLink = new PushLink(this, R.drawable.ic_launcher, 10, "63f9131513fa3991");
		mPushLink.start();
		Log.d(TAG, "PushLink started.");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mPushLink.stop();
		Log.d(TAG, "PushLink stopped.");
	}

}

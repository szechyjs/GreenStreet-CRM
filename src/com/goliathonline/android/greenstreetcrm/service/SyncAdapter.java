package com.goliathonline.android.greenstreetcrm.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.google.android.c2dm.C2DMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
	static final String TAG = "SyncAdapter";
	
	public static final String DEVICE_TYPE = "android";
    public static final String LAST_SYNC = "last_sync";
    public static final String SERVER_LAST_SYNC = "server_last_sync";
    public static final String DM_REGISTERED = "dm_registered";

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	
    	Log.d("Sync--------", "Sync clicked");
    	TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    	String clientDeviceId = tm.getDeviceId();
    	Log.i("device id", clientDeviceId);
    	final long newSyncTime = System.currentTimeMillis();
    	
    	final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
    	final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
    	final boolean initialize = extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);

    	C2DMReceiver.refreshAppC2DMRegistrationState(mContext);
    	
    	Log.i(TAG, "Beginning " + (uploadOnly ? "upload-only" : "full") +
    			" sync for account " + account.name);
    	
    	// Read this account's sync metadata
    	final SharedPreferences syncMeta = mContext.getSharedPreferences("sync:" + account.name, 0);
    	long lastSyncTime = syncMeta.getLong(LAST_SYNC, 0);
    	long lastServerSyncTime = syncMeta.getLong(SERVER_LAST_SYNC, 0);
    	
    	// Check for changes in either app-wide auto sync registration information, or changes in
        // the user's preferences for auto sync on this account; if either changes, piggy back the
        // new registration information in this sync.
        Log.d(".......", "not use C2DMessaging");
        long lastRegistrationChangeTime =C2DMessaging.getLastRegistrationChange(mContext);
    	
    	boolean autoSyncDesired = ContentResolver.getMasterSyncAutomatically() &&
    			ContentResolver.getSyncAutomatically(account, CustomerContract.CONTENT_AUTHORITY);
    	boolean autoSyncEnabled = syncMeta.getBoolean(DM_REGISTERED, false);
    	
    	// Will be 0 for no change, -1 for unregister, 1 for register.
    	final int deviceRegChange;
    	//JsonRpcClient.Call deviceRegCall = null;
    	if (autoSyncDesired != autoSyncEnabled || lastRegistrationChangeTime > lastSyncTime ||
    			initialize || manualSync) {
    		
    		String registrationId = C2DMessaging.getRegistrationId(mContext);
    		Log.d("registrationId->>", registrationId);
    		deviceRegChange = (autoSyncDesired && registrationId != null) ? 1 : -1;
    		
    		if (Log.isLoggable(TAG, Log.DEBUG)) {
    			Log.d(TAG, "Auto sync selection or registration information has changed, " +
    					(deviceRegChange == 1 ? "registering" : "unregistering") +
    					" messaging for this device, for account " + account.name);
    		}
    		
    		try {
    			if (deviceRegChange == 1) {
    				// Register device for auto sync on this account
    				//deviceRegCall
    				JSONObject params = new JSONObject();
    				
    				//DeviceRegistration device = new DeviceRegistration(clientDeviceId,
    				//		DEVICE_TYPE, registrationId);
    				
    				
    			} else {
    				// Unregister device for auto sync on this account
    				JSONObject params = new JSONObject();
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    			return;
    		}
    	} else {
    		deviceRegChange = 0;
    	}
    }
}
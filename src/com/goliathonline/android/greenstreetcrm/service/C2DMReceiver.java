package com.goliathonline.android.greenstreetcrm.service;

import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Receive a push message from the Cloud to Device Messaging (C2DM) service.
 * This class should be modified to include functionality specific to your
 * application. This class must have a no-arg constructor and pass the sender id
 * to the superclass constructor.
 */
public class C2DMReceiver extends C2DMBaseReceiver {
	static final String TAG = "C2DMReceiver";
	
	public static final String SENDER_ID = "jared.szechy@gmail.com";
	public static final String C2DM_SENDER = "jared.szechy@gmail.com";
    public static final String C2DM_ACCOUNT_EXTRA = "account_name";
    public static final String C2DM_MESSAGE_EXTRA = "message";
    public static final String C2DM_MESSAGE_SYNC = "sync";

    public C2DMReceiver() {
        super(SENDER_ID);
    }

    /**
     * Called on registration error. This is called in the context of a Service
     * - no dialog or UI.
     * 
     * @param context the Context
     * @param errorId an error message, defined in {@link C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
    	Toast.makeText(context, "Messaging registration error: " + errorId,
                Toast.LENGTH_LONG).show();
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        String accountName = intent.getExtras().getString(C2DM_ACCOUNT_EXTRA);
        String message = intent.getExtras().getString(C2DM_MESSAGE_EXTRA);
        if (C2DM_MESSAGE_SYNC.equals(message)) {
        	if (accountName != null) {
        		if (Log.isLoggable(TAG, Log.DEBUG)) {
        			Log.d(TAG, "Messaging request received for account " + accountName);
        		}
        		
        		ContentResolver.requestSync(
        				new Account(accountName, CustomerContract.ACCOUNT_TYPE),
        				CustomerContract.CONTENT_AUTHORITY, new Bundle());
        	}
        }
    }
    
    /**
     * Register or unregister based on phone sync settings.
     * Called on each performSync by the SyncAdapter.
     */
    public static void refreshAppC2DMRegistrationState(Context context) {
        // Determine if there are any auto-syncable accounts. If there are, make sure we are
        // registered with the C2DM servers. If not, unregister the application.
        boolean autoSyncDesired = false;
        if (ContentResolver.getMasterSyncAutomatically()) {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(CustomerContract.ACCOUNT_TYPE);
            for (Account account : accounts) {
                if (ContentResolver.getIsSyncable(account, CustomerContract.CONTENT_AUTHORITY) > 0 &&
                        ContentResolver.getSyncAutomatically(account, CustomerContract.CONTENT_AUTHORITY)) {
                    autoSyncDesired = true;
                    break;
                }
            }
        }

        boolean autoSyncEnabled = !C2DMessaging.getRegistrationId(context).equals("");

        if (autoSyncEnabled != autoSyncDesired) {
            Log.i(TAG, "System-wide desirability for GreenStreetCRM auto sync has changed; " +
                    (autoSyncDesired ? "registering" : "unregistering") +
                    " application with C2DM servers.");

            if (autoSyncDesired == true) {
                C2DMessaging.register(context, C2DM_SENDER);
            } else {
                C2DMessaging.unregister(context);
            }
        }
    }
}

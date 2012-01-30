package com.goliathonline.android.greenstreetcrm.ui;

import java.util.ArrayList;
import java.util.List;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AccountList extends ListActivity {
	
	private static Account[] accounts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		accounts = accountManager.getAccountsByType(CustomerContract.ACCOUNT_TYPE);
		setContentView(R.layout.account_list);

		if (accounts.length > 0) {
			String[] names = new String[accounts.length];
			int i = 0;
			for (Account account : accounts) {
				names[i] = account.name;
				i++;
			}
			this.setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item_account, names));
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Account account = accounts[position];
		setSync(account, true, getApplicationContext());
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i] != account)
				ContentResolver.setIsSyncable(accounts[i], CustomerContract.CONTENT_AUTHORITY, 0);
		}
		finish();
	}

	public static Account singleEnabledAccount(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		accounts = accountManager.getAccountsByType(CustomerContract.ACCOUNT_TYPE);
		List<Account> syncableAccounts = new ArrayList<Account>();
		for(Account account : accounts) {
			if (isSyncable(account))
				syncableAccounts.add(account);
		}
		if (syncableAccounts.size() == 1)
			return syncableAccounts.get(0);
		else
			return null;
	}
	
	private static boolean isSyncable(Account account) {
		return ContentResolver.getIsSyncable(account, CustomerContract.CONTENT_AUTHORITY) == 1;
	}
	
	private void setSync(Account account, Boolean enable, Context ctx) {
		ContentResolver.setIsSyncable(account, CustomerContract.CONTENT_AUTHORITY, enable ? 1 : 0);
	}

}

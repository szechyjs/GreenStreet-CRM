package com.goliathonline.android.greenstreetcrm.ui;

import java.util.ArrayList;
import java.util.List;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AccountList extends BaseActivity {
	
	private static Account[] accounts;
	private ListView mListView;
	private TextView mNoAccounts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActivityHelper().setupActionBar(null, 0);
		
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		accounts = accountManager.getAccountsByType(CustomerContract.ACCOUNT_TYPE);
		setContentView(R.layout.account_list);
		mListView = (ListView) findViewById(R.id.account_list);
		mNoAccounts = (TextView) findViewById(R.id.empty_account_list);

		if (accounts.length > 0) {
			String[] names = new String[accounts.length];
			int i = 0;
			for (Account account : accounts) {
				names[i] = account.name;
				i++;
			}
			mNoAccounts.setVisibility(View.GONE);
			mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item_account, names));
			mListView.setOnItemClickListener(onClick);
		}
	}
	
	OnItemClickListener onClick = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> l, View v, int pos,
				long id) {
			Account account = accounts[pos];
			setSync(account, true, getApplicationContext());
			for (int i = 0; i < accounts.length; i++) {
				if (accounts[i] != account)
					ContentResolver.setIsSyncable(accounts[i], CustomerContract.CONTENT_AUTHORITY, 0);
			}
			finish();
		}
	};

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

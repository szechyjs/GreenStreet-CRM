package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
import com.goliathonline.android.greenstreetcrm.ui.phone.CustomerEditActivity;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A {@link ListFragment} showing a list of sandbox comapnies.
 */
public class CustomersFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String STATE_CHECKED_POSITION = "checkedPosition";

    private Cursor mCursor;
    private CursorAdapter mAdapter;
    private int mCheckedPosition = -1;
    private boolean mHasSetEmptyText = false;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        reloadFromArguments(getArguments());
        
        setHasOptionsMenu(true);
    }

    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCheckedPosition = -1;
        setListAdapter(null);

        mHandler.cancelOperation(CustomersQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        final Uri vendorsUri = intent.getData();
        final int vendorQueryToken;

        if (vendorsUri == null) {
            return;
        }

        String[] projection;

        mAdapter = new VendorsAdapter(getActivity());
        projection = CustomersQuery.PROJECTION;
        vendorQueryToken = CustomersQuery._TOKEN;


        setListAdapter(mAdapter);

        // Start background query to load vendors
        mHandler.startQuery(vendorQueryToken, null, vendorsUri, projection, null, null,
                CustomerContract.Customers.DEFAULT_SORT);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (savedInstanceState != null) {
            mCheckedPosition = savedInstanceState.getInt(STATE_CHECKED_POSITION, -1);
        }

        if (!mHasSetEmptyText) {
            // Could be a bug, but calling this twice makes it become visible when it shouldn't
            // be visible.
            setEmptyText(getString(R.string.empty_customers));
            mHasSetEmptyText = true;
        }
    }


    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == CustomersQuery._TOKEN ) {
            onVendorsOrSearchQueryComplete(cursor);
        } else {
            cursor.close();
        }
    }

    /**
     * Handle {@link VendorsQuery} {@link Cursor}.
     */
    private void onVendorsOrSearchQueryComplete(Cursor cursor) {
        if (mCursor != null) {
            // In case cancelOperation() doesn't work and we end up with consecutive calls to this
            // callback.
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        // TODO(romannurik): stopManagingCursor on detach (throughout app)
        mCursor = cursor;
        getActivity().startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
        if (mCheckedPosition >= 0 && getView() != null) {
            getListView().setItemChecked(mCheckedPosition, true);
        }
        registerForContextMenu(getListView());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getContentResolver().registerContentObserver(
                CustomerContract.Customers.CONTENT_URI, true, mVendorChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mVendorChangesObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_menu_items, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_add:
           	
            	if (UIUtils.isHoneycombTablet(getActivity()))
            	{
            		final FragmentTransaction ft = getFragmentManager().beginTransaction();
            		ft.replace(R.id.fragment_container_customer_detail, new CustomerEditFragment());
            		ft.commit();
            	}
            	else
            	{
            		//ft.replace(getId(), new CustomerEditFragment());
            		startActivity(new Intent(getActivity(), CustomerEditActivity.class));
            	}
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Launch viewer for specific vendor.
        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        final String vendorId = cursor.getString(CustomersQuery._ID);
        final Uri vendorUri = CustomerContract.Customers.buildCustomerUri(vendorId);
        ((BaseActivity) getActivity()).openActivityOrFragment(new Intent(Intent.ACTION_VIEW,
                vendorUri));

        getListView().setItemChecked(position, true);
        mCheckedPosition = position;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getActivity().getMenuInflater();
    	inflater.inflate(R.menu.context_customers, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      
      final Cursor cursor = (Cursor)mAdapter.getItem(info.position);
      final String customerId = cursor.getString(CustomersQuery._ID);
      final Uri customerUri = CustomerContract.Customers.buildCustomerUri(customerId);
      
      switch (item.getItemId()) {
      case R.id.edit:
    	  CustomerEditFragment fg = new CustomerEditFragment();
          Bundle args = new Bundle();
          args.putParcelable("_uri", customerUri);
          fg.setArguments(args);
          
          if (UIUtils.isHoneycombTablet(getActivity()))
          {
          	final FragmentTransaction ft = getFragmentManager().beginTransaction();
      		ft.replace(R.id.fragment_container_customer_detail, fg);
      		ft.commit();
          }
      	else
      	{
      		final Intent intent = new Intent(Intent.ACTION_VIEW, customerUri, getActivity().getBaseContext(), CustomerEditActivity.class);
      		//intent.putExtra("_uri", customerUri);
              startActivity(intent);
      	}
        return true;
      case R.id.delete:
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setMessage("Are you sure you want to delete?")
      	       .setCancelable(false)
      	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      	           public void onClick(DialogInterface dialog, int id) {
      	        	 int ret = getActivity().getContentResolver().delete(customerUri, null, null);
      	        	 dialog.dismiss();
      	           }
      	       })
      	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
      	           public void onClick(DialogInterface dialog, int id) {
      	                dialog.cancel();
      	           }
      	       });
          AlertDialog alert = builder.create();
          alert.show();
      	
          
          return true;
      default:
        return super.onContextItemSelected(item);
      }
    }

    public void clearCheckedPosition() {
        if (mCheckedPosition >= 0) {
            getListView().setItemChecked(mCheckedPosition, false);
            mCheckedPosition = -1;
        }
    }

    /**
     * {@link CursorAdapter} that renders a {@link VendorsQuery}.
     */
    private class VendorsAdapter extends CursorAdapter {
        public VendorsAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_customer_oneline,
                    parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.vendor_name)).setText(
                    cursor.getString(CustomersQuery.LASTNAME) + ", "
                    + cursor.getString(CustomersQuery.FIRSTNAME));

            final boolean starred = cursor.getInt(CustomersQuery.STARRED) != 0;
            view.findViewById(R.id.star_button).setVisibility(
                    starred ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private ContentObserver mVendorChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mCursor.requery();
            }
        }
    };

    /**
     * {@link com.google.android.apps.iosched.provider.ScheduleContract.Vendors} query parameters.
     */
    private interface CustomersQuery {
        int _TOKEN = 0x1;

        String[] PROJECTION = {
                BaseColumns._ID,
                CustomerContract.Customers.CUSTOMER_ID,
                CustomerContract.Customers.CUSTOMER_LASTNAME,
                CustomerContract.Customers.CUSTOMER_FIRSTNAME,
                CustomerContract.Customers.CUSTOMER_STARRED,
        };

        int _ID = 0;
        int CUSTOMER_ID = 1;
        int LASTNAME = 2;
        int FIRSTNAME = 3;
        int STARRED = 4;
    }
}

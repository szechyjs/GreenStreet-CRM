package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
import com.goliathonline.android.greenstreetcrm.ui.phone.JobEditActivity;
import com.goliathonline.android.greenstreetcrm.util.NotifyingAsyncQueryHandler;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A {@link ListFragment} showing a list of jobs.
 */
public class JobsFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String STATE_CHECKED_POSITION = "checkedPosition";

    private Bundle mExtras;
    private Cursor mCursor;
    private CursorAdapter mAdapter;
    private String mAction;
    private Uri mJobUri;
    private int mCheckedPosition = -1;
    private boolean mHasSetEmptyText = false;
    private final int mJobQueryToken = JobsQuery._TOKEN;
    private final String[] mJobProjection = JobsQuery.PROJECTION;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        reloadFromArguments(getArguments());

        if (!mJobUri.getLastPathSegment().equals("starred"))
        {
        	setHasOptionsMenu(true);
        
	        ActionBar actionbar = getActivity().getActionBar();
	        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        actionbar.setDisplayShowTitleEnabled(false);
	        
	        Tab all = actionbar.newTab().setText("All Jobs").setTabListener(allJobsTabListener);
	        Tab open = actionbar.newTab().setText("Open Jobs").setTabListener(openJobsTabListener);
	        Tab closed = actionbar.newTab().setText("Closed Jobs").setTabListener(closedJobsTabListener);
	        
	        actionbar.addTab(all);
	        actionbar.addTab(open);
	        actionbar.addTab(closed);
        }
        
        // if this is from the share menu
        if (Intent.ACTION_SEND.equals(mAction)) {
        	if (mExtras.containsKey(Intent.EXTRA_STREAM)) {
        		// Get resource path
        		Uri extraUri = (Uri) mExtras.getParcelable(Intent.EXTRA_STREAM);
        		String filename = UIUtils.parseUriToFilename(extraUri, getActivity());
        		
        		if (filename.isEmpty()) {
        			//moustachify(filename, null);
        		}
        	}
        }
    }

    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCheckedPosition = -1;
        setListAdapter(null);

        mHandler.cancelOperation(JobsQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        mJobUri = intent.getData();

        if (mJobUri == null) {
        	mJobUri = Jobs.CONTENT_URI;
        }
        
        mExtras = intent.getExtras();
        mAction = intent.getAction();

        mAdapter = new JobsAdapter(getActivity());

        setListAdapter(mAdapter);

        // Start background query to load jobs
        mHandler.startQuery(mJobQueryToken, null, mJobUri, mJobProjection, null, null,
                CustomerContract.Jobs.DEFAULT_SORT);
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
            setEmptyText(getString(R.string.empty_jobs));
            mHasSetEmptyText = true;
        }
    }


    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == JobsQuery._TOKEN ) {
            onJobsOrSearchQueryComplete(cursor);
        } else {
            cursor.close();
        }
    }

    /**
     * Handle {@link JobsQuery} {@link Cursor}.
     */
    private void onJobsOrSearchQueryComplete(Cursor cursor) {
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
                CustomerContract.Jobs.CONTENT_URI, true, mJobChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mJobChangesObserver);
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
            		ft.replace(R.id.fragment_container_job_detail, new JobEditFragment());
            		ft.commit();
            	}
            	else
            	{
            		startActivity(new Intent(getActivity(), JobEditActivity.class));
            	}
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Launch viewer for specific job.
        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        final String jobId = cursor.getString(JobsQuery._ID);
        final Uri jobUri = CustomerContract.Jobs.buildJobUri(jobId);
        ((BaseActivity) getActivity()).openActivityOrFragment(new Intent(Intent.ACTION_VIEW,
                jobUri));

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
      final String jobId = cursor.getString(JobsQuery._ID);
      final Uri jobUri = CustomerContract.Jobs.buildJobUri(jobId);
      
      switch (item.getItemId()) {
      case R.id.edit:
    	  JobEditFragment fg = new JobEditFragment();
          Bundle args = new Bundle();
          args.putParcelable("_uri", jobUri);
          fg.setArguments(args);
          
          if (UIUtils.isHoneycombTablet(getActivity()))
          {
          	final FragmentTransaction ft = getFragmentManager().beginTransaction();
      		ft.replace(R.id.fragment_container_job_detail, fg);
      		ft.commit();
          }
      	else
      	{
      		final Intent intent = new Intent(Intent.ACTION_VIEW, jobUri, getActivity().getBaseContext(), JobEditActivity.class);
            startActivity(intent);
      	}
        return true;
      case R.id.delete:
          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setMessage("Are you sure you want to delete?")
      	       .setCancelable(false)
      	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      	           public void onClick(DialogInterface dialog, int id) {
      	        	 getActivity().getContentResolver().delete(jobUri, null, null);
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
    
    private TabListener allJobsTabListener = new TabListener()
    {

		public void onTabReselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
	        final Uri jobsUri = Jobs.CONTENT_URI;

	        // Start background query to load jobs
	        mHandler.startQuery(mJobQueryToken, null, jobsUri, mJobProjection, null, null,
	                CustomerContract.Jobs.DEFAULT_SORT);
		}

		public void onTabUnselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    private TabListener openJobsTabListener = new TabListener()
    {

		public void onTabReselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
	        final Uri jobsUri = Jobs.CONTENT_OPEN_URI;

	        // Start background query to load jobs
	        mHandler.startQuery(mJobQueryToken, null, jobsUri, mJobProjection, null, null,
	                CustomerContract.Jobs.DEFAULT_SORT);
		}

		public void onTabUnselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    private TabListener closedJobsTabListener = new TabListener()
    {

		public void onTabReselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
	        final Uri jobsUri = Jobs.CONTENT_CLOSED_URI;

	        // Start background query to load jobs
	        mHandler.startQuery(mJobQueryToken, null, jobsUri, mJobProjection, null, null,
	                CustomerContract.Jobs.DEFAULT_SORT);
		}

		public void onTabUnselected(Tab arg0,
				android.app.FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
    	
    };

    /**
     * {@link CursorAdapter} that renders a {@link JobsQuery}.
     */
    private class JobsAdapter extends CursorAdapter {
        public JobsAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_job_oneline,
                    parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.job_name)).setText(
                    cursor.getString(JobsQuery.JOB_ID) + " - "
                    + UIUtils.jobStatusToString(cursor.getString(JobsQuery.STATUS)));

            final boolean starred = cursor.getInt(JobsQuery.STARRED) != 0;
            view.findViewById(R.id.star_button).setVisibility(
                    starred ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private ContentObserver mJobChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mCursor.requery();
            }
        }
    };

    /**
     * {@link com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs} query parameters.
     */
    private interface JobsQuery {
        int _TOKEN = 0x1;

        String[] PROJECTION = {
                BaseColumns._ID,
                CustomerContract.Jobs.JOB_ID,
                CustomerContract.Jobs.JOB_DESC,
                CustomerContract.Jobs.JOB_STATUS,
                CustomerContract.Jobs.JOB_CUST_ID,
                CustomerContract.Jobs.JOB_STARRED,
        };

        int _ID = 0;
        int JOB_ID = 1;
        int DESC = 2;
        int STATUS = 3;
        int CUST_ID = 4;
        int STARRED = 5;
    }
}

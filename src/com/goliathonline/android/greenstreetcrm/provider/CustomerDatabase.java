package com.goliathonline.android.greenstreetcrm.provider;

import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Customers;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.CustomersColumns;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.Jobs;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.JobsColumns;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract.SyncColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link CustomerProvider}.
 */
public class CustomerDatabase extends SQLiteOpenHelper {
    private static final String TAG = "CustomerDatabase";

    private static final String DATABASE_NAME = "customers.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_LAUNCH = 2;
    private static final int VER_CHANGE_CUST_ID = 3;
    private static final int VER_ADD_JOBS = 4;
    private static final int VER_CHANGE_JOB_COLS = 5;
    private static final int VER_ADD_JOB_STEPS = 6;

    private static final int DATABASE_VERSION = VER_ADD_JOB_STEPS;

    interface Tables {
        String CUSTOMERS = "customers";
        String JOBS = "jobs";
        String CUSTOMERS_JOBS = "customers_jobs";
        
        String CUSTOMERS_JOBS_JOIN_JOBS = "customers_jobs "
                + "LEFT OUTER JOIN jobs ON customers_jobs.job_id=jobs.job_id";
    }
    
    public interface CustomersJobs {
        String CUSTOMER_ID = "customer_id";
        String JOB_ID = "job_id";
    }
    
    /** {@code REFERENCES} clauses. */
    private interface References {
        String CUSTOMER_ID = "REFERENCES " + Tables.CUSTOMERS + "(" + Customers.CUSTOMER_ID + ")";
        String JOB_ID = "REFERENCES " + Tables.JOBS + "(" + Jobs.JOB_ID + ")";
    }

    public CustomerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.CUSTOMERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + CustomersColumns.CUSTOMER_ID + " TEXT,"
                + CustomersColumns.CUSTOMER_LASTNAME + " TEXT,"
                + CustomersColumns.CUSTOMER_FIRSTNAME + " TEXT,"
                + CustomersColumns.CUSTOMER_COMPANY + " TEXT,"
                + CustomersColumns.CUSTOMER_ADDRESS + " TEXT,"
                + CustomersColumns.CUSTOMER_CITY + " TEXT,"
                + CustomersColumns.CUSTOMER_STATE + " TEXT,"
                + CustomersColumns.CUSTOMER_ZIPCODE + " TEXT,"
                + CustomersColumns.CUSTOMER_PHONE + " TEXT,"
                + CustomersColumns.CUSTOMER_MOBILE + " TEXT,"
                + CustomersColumns.CUSTOMER_EMAIL+ " TEXT,"
                + CustomersColumns.CUSTOMER_STARRED + " INTEGER NOT NULL DEFAULT 0)");
        
        db.execSQL("CREATE TABLE " + Tables.JOBS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + JobsColumns.JOB_ID + " TEXT,"
                + JobsColumns.JOB_CUST_ID + " TEXT,"
                + JobsColumns.JOB_TITLE + " TEXT,"
                + JobsColumns.JOB_DESC + " TEXT,"
                + JobsColumns.JOB_STATUS + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_DUE + " TEXT,"
                + JobsColumns.JOB_STEP1 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP2 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP3 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP4 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP5 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP6 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP7 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP8 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP9 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STEP10 + " INTEGER NOT NULL DEFAULT 0,"
                + JobsColumns.JOB_STARRED + " INTEGER NOT NULL DEFAULT 0)");
        
        db.execSQL("CREATE TABLE " + Tables.CUSTOMERS_JOBS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CustomersJobs.CUSTOMER_ID + " TEXT NOT NULL " + References.CUSTOMER_ID + ","
                + CustomersJobs.JOB_ID + " TEXT NOT NULL " + References.JOB_ID + ","
                + "UNIQUE (" + CustomersJobs.CUSTOMER_ID + ","
                        + CustomersJobs.JOB_ID + ") ON CONFLICT REPLACE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // NOTE: This switch statement is designed to handle cascading database
        // updates, starting at the current version and falling through to all
        // future upgrade cases. Only use "break;" when you want to drop and
        // recreate the entire database.
        int version = oldVersion;

        switch (version) {
            case VER_CHANGE_JOB_COLS:
            	db.execSQL("ALTER TABLE " + Tables.JOBS + " ADD COLUMN "
                        + JobsColumns.JOB_STEP6 + " INTEGER NOT NULL DEFAULT 0");
            	db.execSQL("ALTER TABLE " + Tables.JOBS + " ADD COLUMN "
                        + JobsColumns.JOB_STEP7 + " INTEGER NOT NULL DEFAULT 0");
            	db.execSQL("ALTER TABLE " + Tables.JOBS + " ADD COLUMN "
                        + JobsColumns.JOB_STEP8 + " INTEGER NOT NULL DEFAULT 0");
            	db.execSQL("ALTER TABLE " + Tables.JOBS + " ADD COLUMN "
                        + JobsColumns.JOB_STEP9 + " INTEGER NOT NULL DEFAULT 0");
            	db.execSQL("ALTER TABLE " + Tables.JOBS + " ADD COLUMN "
                        + JobsColumns.JOB_STEP10 + " INTEGER NOT NULL DEFAULT 0");
            	
            	version = VER_ADD_JOB_STEPS;

        }

        Log.d(TAG, "after upgrade logic, at version " + version);
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "Destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.CUSTOMERS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.JOBS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CUSTOMERS_JOBS);

            onCreate(db);
        }
    }
}

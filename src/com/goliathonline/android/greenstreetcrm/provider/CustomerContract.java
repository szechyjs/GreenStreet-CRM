package com.goliathonline.android.greenstreetcrm.provider;

//import com.google.android.apps.iosched.util.ParserUtils;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;
import com.goliathonline.android.greenstreetcrm.util.ParserUtils;
/**
 * Contract class for interacting with {@link CustomerProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri} are generated
 * using stronger {@link String} identifiers, instead of {@code int}
 * {@link BaseColumns#_ID} values, which are prone to shuffle during sync.
 */
public class CustomerContract {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    interface CustomersColumns {
        /** Unique string identifying this customer. */
        String CUSTOMER_ID = "customer_id";
        /** Last name of this customer. */
        String CUSTOMER_LASTNAME = "customer_lastname";
        /** First name of this customer. */
        String CUSTOMER_FIRSTNAME = "customer_firstname";
        /** Company this customer works for. */
        String CUSTOMER_COMPANY = "customer_company";
        /** Address of this customer. */
        String CUSTOMER_ADDRESS = "customer_address";
        /** City of this customer. */
        String CUSTOMER_CITY = "customer_city";
        /** State of this customer. */
        String CUSTOMER_STATE = "customer_state";
        /** Zipcode of this customer. */
        String CUSTOMER_ZIPCODE = "customer_zip";
        /** Home phone of this customer. */
        String CUSTOMER_PHONE = "customer_phone";
        /** Mobile phone of this customer. */
        String CUSTOMER_MOBILE = "customer_mobile";
        /** Email address of this customer. */
        String CUSTOMER_EMAIL = "customer_email";
        /** Starred customer. */
        String CUSTOMER_STARRED = "customer_starred";

    }

    interface JobsColumns {
        /** Unique string identifying this job. */
        String JOB_ID = "job_id";
        /** ID of the customer */
        String JOB_CUST_ID = "job_cust_id";
        /** Title describing this job. */
        String JOB_TITLE = "job_title";
        /** Job description. */
        String JOB_DESC = "job_desc";
        /** Status of the job */
        String JOB_STATUS = "job_status";
        /** Due date of job */
        String JOB_DUE = "job_due";
        /** Step 1 status */
        String JOB_STEP1 = "job_step1";
        /** Step 2 status */
        String JOB_STEP2 = "job_step2";
        /** Step 3 status */
        String JOB_STEP3 = "job_step3";
        /** Step 4 status */
        String JOB_STEP4 = "job_step4";
        /** Step 5 status */
        String JOB_STEP5 = "job_step5";
        /** Step 6 status */
        String JOB_STEP6 = "job_step6";
        /** Step 7 status */
        String JOB_STEP7 = "job_step7";
        /** Step 8 status */
        String JOB_STEP8 = "job_step8";
        /** Step 9 status */
        String JOB_STEP9 = "job_step9";
        /** Step 10 status */
        String JOB_STEP10 = "job_step10";
        /** User-specific flag indicating starred status. */
        String JOB_STARRED = "job_starred";
    }

    interface MemosColumns {
        /** Unique string identifying this memo. */
        String MEMO_ID = "memo_id";
        /** ID of the job. */
        String MEMO_JOB_ID = "memo_job_id";
        /** Section ID */
        String MEMO_SECT_ID = "memo_sect_id";
        /** Memo text */
        String MEMO_TEXT = "memo_text";
        /** User-specific flag indicating starred status. */
        String MEMO_STARRED = "memo_starred";
    }

    public static final String CONTENT_AUTHORITY = "com.goliathonline.android.greenstreetcrm";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_STARRED = "starred";
    private static final String PATH_CUSTOMERS = "customers";
    private static final String PATH_JOBS = "jobs";
    private static final String PATH_OPEN = "open";
    private static final String PATH_CLOSED = "closed";
    private static final String PATH_MEMOS = "memos";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";


    /**
     * Customers are individual people that lead {@link Jobs}.
     */
    public static class Customers implements CustomersColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUSTOMERS).build();
        public static final Uri CONTENT_STARRED_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.greenstreetcrm.customer";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.greenstreetcrm.customer";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = CustomersColumns.CUSTOMER_LASTNAME
                + " COLLATE NOCASE ASC";

        /** Build {@link Uri} for requested {@link #CUSTOMER_ID}. */
        public static Uri buildCustomerUri(String customerId) {
            return CONTENT_URI.buildUpon().appendPath(customerId).build();
        }

        /** Read {@link #CUSTOMER_ID} from {@link Customers} {@link Uri}. */
        public static String getCustomerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Build {@link Uri} that references any {@link Jobs} associated
         * with the requested {@link #CUSTOMER_ID}.
         */
        public static Uri buildJobsDirUri(String customerId) {
            return CONTENT_URI.buildUpon().appendPath(customerId).appendPath(PATH_JOBS).build();
        }

        /**
         * Generate a {@link #CUSTOMER_ID} that will always match the requested
         * {@link Customers} details.
         */
        public static String generateCustomerId(String customerLdap) {
            return ParserUtils.sanitizeId(customerLdap);
        }
    }

    /**
     * Each job is a block of time that has a {@link Tracks}, a
     * {@link Rooms}, and zero or more {@link Speakers}.
     */
    public static class Jobs implements JobsColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOBS).build();
        public static final Uri CONTENT_STARRED_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();
        public static final Uri CONTENT_OPEN_URI =
        		CONTENT_URI.buildUpon().appendPath(PATH_OPEN).build();
        public static final Uri CONTENT_CLOSED_URI =
        		CONTENT_URI.buildUpon().appendPath(PATH_CLOSED).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.greenstreetcrm.job";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.greenstreetcrm.job";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = JobsColumns.JOB_ID
        		+ " COLLATE NOCASE ASC";

        /** Build {@link Uri} for requested {@link #JOB_ID}. */
        public static Uri buildJobUri(String jobId) {
            return CONTENT_URI.buildUpon().appendPath(jobId).build();
        }

        /** Read {@link #JOB_ID} from {@link Jobs} {@link Uri}. */
        public static String getJobId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Generate a {@link #JOB_ID} that will always match the requested
         * {@link Jobs} details.
         */
        public static String generateJobId(String title) {
            return ParserUtils.sanitizeId(title);
        }

        public static enum Status { OPEN(0), CLOSED(1);

        	private int code;

        	private Status(int c) {
        		code = c;
        	}

        	public int getCode() {
        		return code;
        	}

        	@Override public String toString() {
        	   String s = super.toString();
        	   return s.substring(0, 1) + s.substring(1).toLowerCase();
        	 }
        };
    }

    /**
     * Memos are individual messages that apply to  {@link Jobs}.
     */
    public static class Memos implements MemosColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMOS).build();
        public static final Uri CONTENT_STARRED_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.greenstreetcrm.memo";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.greenstreetcrm.memo";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = MemosColumns.MEMO_ID
                + " COLLATE NOCASE ASC";

        /** Build {@link Uri} for requested {@link #CUSTOMER_ID}. */
        public static Uri buildMemoUri(String memoId) {
            return CONTENT_URI.buildUpon().appendPath(memoId).build();
        }

        /** Read {@link #MEMO_ID} from {@link Memos} {@link Uri}. */
        public static String getMemoId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getJobId(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        /**
         * Generate a {@link #CUSTOMER_ID} that will always match the requested
         * {@link Customers} details.
         */
        public static String generateMemoId(String memoId) {
            return ParserUtils.sanitizeId(memoId);
        }
    }

    public static class SearchSuggest {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();

        public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
                + " COLLATE NOCASE ASC";
    }

    private CustomerContract() {
    }
}

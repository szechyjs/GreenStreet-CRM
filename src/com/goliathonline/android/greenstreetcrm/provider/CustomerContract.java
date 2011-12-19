package com.goliathonline.android.greenstreetcrm.provider;

//import com.google.android.apps.iosched.util.ParserUtils;

import android.app.SearchManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

import java.util.List;

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

    public static final String CONTENT_AUTHORITY = "com.goliathonline.android.greenstreetcrm";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_STARRED = "starred";
    private static final String PATH_CUSTOMERS = "customers";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";


    /**
     * Customers are individual people that lead {@link Sessions}.
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
         * Generate a {@link #CUSTOMER_ID} that will always match the requested
         * {@link Customers} details.
         */
        public static String generateCustomerId(String customerLdap) {
            return ParserUtils.sanitizeId(customerLdap);
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

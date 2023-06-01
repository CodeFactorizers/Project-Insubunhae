package com.sgcd.insubunhae.db;

import android.net.Uri;
import android.provider.BaseColumns;
public class DBContract {

    /* Do not allow this class to be instantiated */
    private DBContract() {}

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "i_contacts.db";
    /*
     * public static final String NOT_NULL = "NOT NULL";
     * public static final String COMMA = ",";
     */
    public static final String AUTHORITY = "com.sgcd.insubunhae";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";

    public static final int ARRAY_LENGTH = 8;

    /* An array list of all the SQL create table statements */
    public static final String[] SQL_CREATE_TABLE_ARRAY = {
            MainContacts.CREATE_TABLE,
            Analysis.CREATE_TABLE,
            Memo.CREATE_TABLE,
            MessengerHistory.CREATE_TABLE,
            CallLog.CREATE_TABLE,
            GroupInfo.CREATE_TABLE,
            GroupMember.CREATE_TABLE,
            Schedule.CREATE_TABLE
    };
    /* An array list of all the SQL create table statements */
    public static final String[] TABLE_NAME_ARRAY = {
            MainContacts.TABLE_NAME,
            Analysis.TABLE_NAME,
            Memo.TABLE_NAME,
            MessengerHistory.TABLE_NAME,
            CallLog.TABLE_NAME,
            GroupInfo.TABLE_NAME,
            GroupMember.TABLE_NAME,
            Schedule.TABLE_NAME
    };

    //public static final class 테이블이름 implements BaseColumns {
    public static final class MainContacts implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private MainContacts() {
        }

        public static final String TABLE_NAME = "MAIN_CONTACTS";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String NAME = "name";
        public static final String FIRST_CHAR = "first_char";
        public static final String PHONE1 = "phone_number1";
        public static final String PHONE_TYPE1 = "phone_number_type1";
        public static final String PHONE2 = "phone_number2";
        public static final String PHONE_TYPE2 = "phone_number_type2";
        public static final String PHONE3 = "phone_number3";
        public static final String PHONE_TYPE3 = "phone_number_type3";
        public static final String IS_GROUPED = "is_grouped";
        public static final String GROUP_CNT = "group_count";
        public static final String ADDRESS = "address1";
        public static final String ADDRESS_TYPE1 = "address_type1";
        public static final String ADDRESS2 = "address2";
        public static final String ADDRESS_TYPE2 = "address_type2";
        public static final String EMAIL = "email";
        public static final String EMAIL2 = "sub_email";
        public static final String WORK = "work";
        public static final String SNS_ID = "sns_id";
        public static final String UPDATED_DATE = "updated_date";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = KEY_CONTACT_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + KEY_CONTACT_ID + " INTEGER,"
                + NAME + " VARCHAR(30) NOT NULL,"
                + FIRST_CHAR + " TEXT,"
                + PHONE1 + " VARCHAR(15),"
                + PHONE_TYPE1 + " TEXT,"
                + PHONE2 + " VARCHAR(15),"
                + PHONE_TYPE2 + " TEXT,"
                + PHONE3 + " VARCHAR(15),"
                + PHONE_TYPE3 + " TEXT,"
                + IS_GROUPED + " INTEGER NOT NULL,"
                + GROUP_CNT + " INTEGER,"
                + ADDRESS + " VARCHAR(50),"
                + ADDRESS_TYPE1 + " TEXT,"
                + ADDRESS2 + " VARCHAR(30),"
                + ADDRESS_TYPE2 + " TEXT,"
                + EMAIL + " VARCHAR(30) CHECK (" + EMAIL + " like '%@%'),"

                + EMAIL2 + " VARCHAR(30) CHECK (" + EMAIL2 + " like '%@%'),"
                + WORK + " VARCHAR(20),"
                + SNS_ID + " VARCHAR(20),"
                + UPDATED_DATE + " INTEGER DEFAULT 0,"
                + "PRIMARY KEY(" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                KEY_CONTACT_ID,
                NAME,
                FIRST_CHAR,
                PHONE1,
                PHONE_TYPE1,
                PHONE2,
                PHONE_TYPE2,
                PHONE3,
                PHONE_TYPE3,
                IS_GROUPED,
                GROUP_CNT,
                ADDRESS,
                ADDRESS_TYPE1,
                ADDRESS2,
                ADDRESS_TYPE2,
                EMAIL,
                EMAIL2,
                WORK,
                SNS_ID,
                UPDATED_DATE
        };
    }
    public static final class Analysis implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private Analysis() {
        }

        public static final String TABLE_NAME = "ANALYSIS";
        public static final String FK_TABLE_NAME = "MAIN_CONTACTS";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String USER_FAM = "user_fam";
        public static final String CALC_FAM = "calc_fam";
        public static final String RECENT_CONTACT = "recent_contact";
        public static final String FIRST_CONTACT = "first_contact";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = KEY_CONTACT_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + USER_FAM + " INTEGER,"
                + CALC_FAM + " INTEGER,"
                + RECENT_CONTACT + " INTEGER,"
                + FIRST_CONTACT + " INTEGER,"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                KEY_CONTACT_ID,
                USER_FAM,
                CALC_FAM,
                RECENT_CONTACT,
                FIRST_CONTACT
        };
    }
    public static final class Memo implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private Memo() {
        }
        public static final String TABLE_NAME = "MEMO";
        public static final String FK_TABLE_NAME = "MAIN_CONTACTS";

        public static final String MEMO_ID = "memo_id";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String MEMO_CONTENTS = "memo_contents";
        public static final String DATETIME = "datetime";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = MEMO_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + MEMO_ID + " INTEGER,"
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + MEMO_CONTENTS + " TEXT,"
                + DATETIME + " INTEGER NOT NULL,"
                + "PRIMARY KEY(" + MEMO_ID + "),"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                MEMO_ID,
                KEY_CONTACT_ID,
                MEMO_CONTENTS,
                DATETIME
        };
    }
    public static final class MessengerHistory implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private MessengerHistory() {
        }
        public static final String TABLE_NAME = "MESSENGER_HISTORY";
        public static final String FK_TABLE_NAME = "MAIN_CONTACTS";
        public static final String HISTORY_ID = "history_id";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String DATETIME = "datetime";
        public static final String DAY = "day";
        public static final String TYPE = "type";
        public static final String COUNT = "count";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = HISTORY_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + HISTORY_ID + " INTEGER,"
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + DATETIME + " INTEGER NOT NULL,"
                + DAY + " VARCHAR(10) NOT NULL,"
                + TYPE + " VARCHAR(10) NOT NULL,"
                + COUNT + " INTEGER,"
                + "PRIMARY KEY(" + HISTORY_ID + "),"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                HISTORY_ID,
                KEY_CONTACT_ID,
                DATETIME,
                DAY,
                TYPE,
                COUNT
        };
    }
    public static final class CallLog implements BaseColumns {
        public static long last_updated = 0L;// to check if callLog update needed. note that it's last_update'd'
        public static int call_log_cnt = 0;

        /* Do not allow this class to be instantiated */
        private CallLog() {
        }
        public static final String TABLE_NAME = "CALL_LOG";
        public static final String FK_TABLE_NAME = "MAIN_CONTACTS";

        public static final String HISTORY_ID = "log_id";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String DATETIME = "datetime";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String TYPE = "type";
        public static final String DURATION = "duration";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = HISTORY_ID + " ASC";



        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + DATETIME + " INTEGER NOT NULL,"
                + NAME + " VARCHAR(30),"
                + PHONE + " VARCHAR(15) NOT NULL,"
                + TYPE + " INTEGER NOT NULL,"
                + DURATION + " INTEGER NOT NULL,"
                //+ "PRIMARY KEY(" + HISTORY_ID + "),"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                HISTORY_ID,
                KEY_CONTACT_ID,
                DATETIME,
                NAME,
                PHONE,
                TYPE,
                DURATION
        };
    }
    public static final class GroupInfo implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private GroupInfo() {
        }
        public static final String TABLE_NAME = "GROUP_INFO";

        public static final String GROUP_ID = "group_id";
        public static final String GROUP_NAME = "group_name";
        public static final String MEMBER_CNT = "member_count";
        public static final String PARENT = "parent_group";
        public static final String CHILD = "child_group";
        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = GROUP_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + GROUP_ID + " INTEGER,"
                + GROUP_NAME + " INTEGER NOT NULL,"
                + MEMBER_CNT + " INTEGER NOT NULL,"
                + PARENT + " VARCHAR(30),"
                + CHILD + " VARCHAR(15),"
                + "PRIMARY KEY(" + GROUP_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                GROUP_ID,
                GROUP_NAME,
                MEMBER_CNT,
                PARENT,
                CHILD
        };
    }
    public static final class GroupMember implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private GroupMember() {
        }
        public static final String TABLE_NAME = "GROUP_MEMBER";
        public static final String FK1_TABLE_NAME = "MAIN_CONTACTS";
        public static final String FK2_TABLE_NAME = "GROUP_INFO";

        public static final String MEMBER_ID = "member_id";
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String GROUP_ID = "group_id";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = MEMBER_ID + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + MEMBER_ID + " INTEGER,"
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + GROUP_ID + " INTEGER NOT NULL,"
                + "PRIMARY KEY(" + MEMBER_ID + "),"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK1_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + "FOREIGN KEY(" + GROUP_ID + ") REFERENCES " + FK2_TABLE_NAME + " (" + GROUP_ID + ")"
                + "FOREIGN KEY(" + GROUP_ID + ") REFERENCES GROUP_INFO (" + GROUP_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                MEMBER_ID,
                KEY_CONTACT_ID,
                GROUP_ID
        };
    }
    public static final class Schedule implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private Schedule() {
        }
        public static final String TABLE_NAME = "SCHEDULE";
        public static final String FK_TABLE_NAME = "MAIN_CONTACTS";

        public static final String SCHEDULE_id = "schedule_id";
        public static final String DATETIME = "datetime";
        public static final String COMPLETE = "complete";
        public static final String DISMISS_CNT = "dismiss_count";
        public static final String KEY_CONTACT_ID = "contact_id";

        /*
         * URI definitions
         */
        //The content style URI
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        //The content URI base for a single row. An ID must be appended.
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        //The default sort order for this table
        public static final String DEFAULT_SORT_ORDER = SCHEDULE_id + " ASC";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + SCHEDULE_id + " INTEGER,"
                + DATETIME + " INTEGER NOT NULL,"
                + COMPLETE + " INTEGER NOT NULL,"
                + DISMISS_CNT + " INTEGER NOT NULL,"
                + KEY_CONTACT_ID + " INTEGER NOT NULL,"
                + "PRIMARY KEY(" + SCHEDULE_id + "),"
                + "FOREIGN KEY(" + KEY_CONTACT_ID + ") REFERENCES " + FK_TABLE_NAME + " (" + KEY_CONTACT_ID + ")"
                + ");";

        //SQL statement to delete the table
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Array of all the columns. Makes for cleaner code
        public static final String[] ATTRIBUTE_ARRAY = {
                SCHEDULE_id,
                DATETIME,
                COMPLETE,
                DISMISS_CNT,
                KEY_CONTACT_ID
        };
    }
}

package com.sgcd.insubunhae.db;

import static android.content.ContentValues.TAG;
import static com.sgcd.insubunhae.db.DBContract.ARRAY_LENGTH;
import static com.sgcd.insubunhae.db.DBContract.SQL_CREATE_TABLE_ARRAY;
import static com.sgcd.insubunhae.db.DBContract.TABLE_NAME_ARRAY;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.sgcd.insubunhae.MainActivity;

import android.content.ContentResolver;
import android.widget.Toast;

import com.sgcd.insubunhae.db.ContactsList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;


public class DBHelper extends SQLiteOpenHelper {
    private static Context context;
    private ContactsList contacts_list = new ContactsList();
    int i = 1; //MESSENGER_HISTORY history_id는 1부터 시작.

    int lastCallLogId = 0;

    public DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
        DBHelper.context = context;
        Log.d("Database Operations", "Database created...");

        //dbDeleteForTest();
    }

    // Delete the database file(for test)
    public void dbDeleteForTest() {
        String databaseName = "i_contacts.db";
        boolean isDeleted = context.deleteDatabase(databaseName);

        if (isDeleted) {
            Log.d(TAG, "Database deleted successfully");
        } else {
            Log.d(TAG, "Failed to delete the database");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            db.execSQL(SQL_CREATE_TABLE_ARRAY[i]);
            Log.d("Database Operations", "Table : " + TABLE_NAME_ARRAY[i] + " created...");
        }

        contacts_list.getContacts(context);
        contacts_list.dbInsert(db);

        smsFromDeviceToDB(db);
        callLogFromDeviceToDB(db);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Create tables
        for (int i = 0; i < ARRAY_LENGTH; i++) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ARRAY[i]);
            Log.d("Database Operations", "Table : " + TABLE_NAME_ARRAY[i] + " dropped...");
        }
        onCreate(db);
    }

    public void callLogFromDeviceToDB(SQLiteDatabase db) {
        int callLogId = lastCallLogId;        // Call log ID
        int contactId = 0;        // Contact ID
        long callDatetime = 0;    // Call date and time
        String contactName;  // Contact name
        String contactPhone = ""; // Contact phone number
        int callType = 0;         // Call type
        int callDuration = 0;     // Call duration

        String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION
        };

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                null
                //CallLog.Calls.DATE + " DESC"
        );

        if (cursor == null) {
            Log.d("callLogFromDeviceToDB", "cursor is null..");
            return;
        }

        if (cursor.moveToFirst()) {
            do {
                // first, get phone number to know if this is saved number
                int phoneIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                if (phoneIndex >= 0) {
                    contactPhone = cursor.getString(phoneIndex);
                }

                ContactInfo contactInfo = getContactInfo(contactPhone);

                // if number is not saved, contactName will be "null"
                contactName = contactInfo.getName();
                contactId = contactInfo.getId();

                //don't insert calllog if name is null
                if (contactName == null) {
                    Log.d("skip", "    <skip> number is not saved in contacts list");
                }
                //if number is saved, let's insert the call log
                else {
                    callLogId++;

                    int datetimeIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                    if (datetimeIndex >= 0) {
                        callDatetime = cursor.getLong(datetimeIndex);
                    }

                    int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    if (typeIndex >= 0) {
                        callType = cursor.getInt(typeIndex);
                    }

                    int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
                    if (durationIndex >= 0) {
                        callDuration = cursor.getInt(durationIndex);
                    }

                    Log.d("callLogFromDeviceToDB", "callLogId: " + callLogId + "\t\t\t contactID: " + contactId + "\t name: " + contactName + "\t phone: " + contactPhone);
                    Log.d("callLogFromDeviceToDB", "datetime: " + callDatetime + "\t type: " + callType + "\t\t duration: " + callDuration);
                    Log.d(TAG, "\n");

                    /* Insert call log to DB */
                    ContentValues values = new ContentValues();
                    values.put(DBContract.CallLog.HISTORY_ID, callLogId);
                    values.put(DBContract.CallLog.KEY_CONTACT_ID, contactId);
                    values.put(DBContract.CallLog.DATETIME, callDatetime);
                    values.put(DBContract.CallLog.NAME, contactName);
                    values.put(DBContract.CallLog.PHONE, contactPhone);
                    values.put(DBContract.CallLog.TYPE, callType);
                    values.put(DBContract.CallLog.DURATION, callDuration);

                    db.insert(DBContract.CallLog.TABLE_NAME, null, values);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public void smsFromDeviceToDB(SQLiteDatabase db) {
        //Log.d("smsFromDeviceToDB", "sms 1");
        int smsHistoryId = 0;   //기록 번호
        int smsContactId = -1;   //연락처 고유 아이디
        long smsDatetime = 0;    //연락 날짜
        String smsDay = "";     //연락 요일
        String smsType = "";    //연락 수단
        int smsCount = -1;       //일 연락 횟수(동일 연락 수단)

        Uri messagesUri = Uri.parse("content://sms/sent");
        Cursor cursor = context.getContentResolver().query(messagesUri, null, null, null, null);

        Cursor cursor1 = null;
        Cursor cursor2 = null;

        if (cursor == null) Log.d("getSmsFromDeviceToDB", "cursor is null..");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    //Log.d("getSmsFromDeviceToDB", "---------");
                    /* get sms from device */
                    smsHistoryId = i++;
                    //Log.d("getSmsFromDeviceToDB", "smsHistoryId : " + smsHistoryId);

                    int senderIndex = cursor.getColumnIndex("address");
                    String smsSender = null;
                    if (senderIndex >= 0) {
                        smsSender = cursor.getString(senderIndex);
                        try {
                            String query = "SELECT contact_id FROM MAIN_CONTACTS WHERE phone_number1 = '"
                                    + smsSender + "'";
                            cursor1 = db.rawQuery(query, null);

                            if (cursor1 != null) {
                                if (cursor1.moveToFirst()) {
                                    int columnIndex = cursor1.getColumnIndex("contact_id");
                                    if (columnIndex >= 0) {
                                        smsContactId = cursor1.getInt(columnIndex);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor1.close();
                    //Log.d("getSmsFromDeviceToDB", "smsContactId : " + smsContactId);

                    int dateIndex = cursor.getColumnIndex("date");
                    Long dateAndTime = null;
                    if (dateIndex >= 0) {
                        dateAndTime = cursor.getLong(dateIndex);
                    }
                    Date date = new Date(dateAndTime);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    try {
                        smsDatetime = dateFormat.parse(dateFormat.format(date)).getTime();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    smsDay = getDayOfDatetime(dateAndTime);
                    smsType = "Sms"; //연락 수단

                    //Log.d("getSmsFromDeviceToDB", "smsDatetime : " + smsDatetime);
                    //Log.d("getSmsFromDeviceToDB", "smsDay : " + smsDay);
                    //Log.d("getSmsFromDeviceToDB", "smsType : " + smsType);

                    //MESSENGER_HISTORY count 체크
                    String attributeValue = null; //없으면 null 유지, 이미 있으면 count값이 들어감.
                    try {
                        String query = "SELECT count FROM MESSENGER_HISTORY " + "WHERE (contact_id = " + smsContactId +
                                " AND type = '" + smsType + "' AND datetime = " + smsDatetime + ")";
                        cursor2 = db.rawQuery(query, null);

                        if (cursor2 != null && cursor2.moveToFirst()) {
                            int columnIndex = cursor2.getColumnIndex("count");
                            attributeValue = cursor2.getString(columnIndex);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cursor2.close();

                    if (attributeValue == null) { //없어서 새로 추가
                        smsCount = 1; //시작이 1개
                        //Log.d("smsFromDeviceToDB", "smsCount : " + smsCount);

                        /* insert sms to db */
                        ContentValues values = new ContentValues();
                        values.put("history_id", smsHistoryId);
                        values.put("contact_id", smsContactId);
                        values.put("datetime", smsDatetime);
                        values.put("day", smsDay);
                        values.put("type", smsType);
                        values.put("count", smsCount);
                        db.insert("MESSENGER_HISTORY", null, values);
                    } else if (attributeValue != null) { //있어서 count만 ++하기
                        int new_count = Integer.parseInt(attributeValue) + 1;

                        String query = "UPDATE Messenger_History SET count = " + new_count + " WHERE contact_id = " + smsContactId + " AND type = '" + smsType + "' AND datetime = " + smsDatetime;
                        db.execSQL(query);

                        //Log.d("smsFromDeviceToDB", "smsCount : " + new_count);
                    }


                } while (cursor.moveToNext());
            } else {
                Log.d("smsFromDeviceToDB", "cursor move to first failed");
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    // from here, some additional methods..
    private static ContactInfo getContactInfo(String phoneNumber) {
        ContactInfo contactInfo = new ContactInfo();
        Cursor contactCursor = null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            contactCursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (contactCursor != null && contactCursor.moveToFirst()) {
                int idIndex = contactCursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                int nameIndex = contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                if (idIndex >= 0) {
                    contactInfo.setId(contactCursor.getInt(idIndex));
                }

                if (nameIndex >= 0) {
                    contactInfo.setName(contactCursor.getString(nameIndex));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contactCursor != null) {
                contactCursor.close();
            }
        }

        return contactInfo;
    }

    public static class ContactInfo {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public String getDayOfDatetime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Sun";
            case Calendar.MONDAY:
                return "Mon";
            case Calendar.TUESDAY:
                return "Tue";
            case Calendar.WEDNESDAY:
                return "Wed";
            case Calendar.THURSDAY:
                return "Thu";
            case Calendar.FRIDAY:
                return "Fri";
            case Calendar.SATURDAY:
                return "Sat";
            default:
                return "";
        }
    }

    public List<String> getAttributeValueFromTable(String tableName, String attributeName, String condition) {
        List<String> attributeValues = new ArrayList<>();

        SQLiteDatabase idb = getWritableDatabase();
        Cursor dbCursor = null;

        try {
            String query = "SELECT " + attributeName + " FROM " + tableName + " WHERE " + condition;
            dbCursor = idb.rawQuery(query, null);
            Log.d("StatisticsFragment", "query : " + query);

            if (dbCursor != null) {
                while (dbCursor.moveToNext()) {
                    int columnIndex = dbCursor.getColumnIndex(attributeName);
                    String attributeValue = dbCursor.getString(columnIndex);
                    attributeValues.add(attributeValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbCursor != null) {
                dbCursor.close();
            }
        }
        idb.close();

        return attributeValues;
    }

    public List<Long> getLongFromTable(String tableName, String attributeName, String condition) {
        List<Long> attributeValues = new ArrayList<>();

        SQLiteDatabase idb = getWritableDatabase();
        Cursor dbCursor = null;

        try {
            String query = "SELECT " + attributeName + " FROM " + tableName + " WHERE " + condition;
            dbCursor = idb.rawQuery(query, null);
            Log.d("StatisticsFragment", "query : " + query);

            if (dbCursor != null) {
                while (dbCursor.moveToNext()) {
                    int columnIndex = dbCursor.getColumnIndex(attributeName);
                    Long attributeValue = dbCursor.getLong(columnIndex);
                    attributeValues.add(attributeValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbCursor != null) {
                dbCursor.close();
            }
        }
        idb.close();

        return attributeValues;
    }

    // DB 집계함수-SUM
    public int getSumOfAttribute() {
        int sum = 0;

        SQLiteDatabase idb = this.getReadableDatabase();

        String query = "SELECT SUM(attributeName) FROM tableName";
        Cursor dbCursor = idb.rawQuery(query, null);

        if (dbCursor.moveToFirst()) {
            sum = dbCursor.getInt(0);
        }

        dbCursor.close();
        idb.close();

        return sum;
    }

    // DB 집계함수-MAX
    public Long getMaxOfAttribute(String tableName, String attributeName) {
        SQLiteDatabase idb = this.getReadableDatabase();

        String query = "SELECT MAX(" + attributeName + ") FROM " + tableName;
        Cursor dbCursor = idb.rawQuery(query, null);
        Long maxValue = null;
        if (dbCursor.moveToFirst()) {
            int columnIndex = dbCursor.getColumnIndex("MAX(" + attributeName + ")");
            if (columnIndex >= 0) {
                maxValue = dbCursor.getLong(columnIndex);
            }
        }
        dbCursor.close();
        return maxValue;
    }

    // DB 집계함수-MIN
    public Long getMinOfAttribute(String tableName, String attributeName) {
        SQLiteDatabase idb = this.getReadableDatabase();

        String query = "SELECT MIN(" + attributeName + ") FROM " + tableName;
        Cursor dbCursor = idb.rawQuery(query, null);
        Long minValue = null;
        if (dbCursor.moveToFirst()) {
            int columnIndex = dbCursor.getColumnIndex("MIN(" + attributeName + ")");
            if (columnIndex >= 0) {
                minValue = dbCursor.getLong(columnIndex);
            }
        }
        dbCursor.close();
        return minValue;
    }
}

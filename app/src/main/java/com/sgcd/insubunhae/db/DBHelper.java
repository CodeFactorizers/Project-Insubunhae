package com.sgcd.insubunhae.db;

import static com.sgcd.insubunhae.db.DBContract.ARRAY_LENGTH;
import static com.sgcd.insubunhae.db.DBContract.SQL_CREATE_TABLE_ARRAY;
import static com.sgcd.insubunhae.db.DBContract.TABLE_NAME_ARRAY;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.sgcd.insubunhae.db.ContactsList;

import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper  {
    private Context context;
    private ContactsList contacts_list = new ContactsList();
    int i = 0;

    public DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
        this.context = context;
        Log.d("Database Operations", "Database created...");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables
        for(int i=0; i<ARRAY_LENGTH; i++) {
            db.execSQL(SQL_CREATE_TABLE_ARRAY[i]);
            Log.d("Database Operations", "Table : " + TABLE_NAME_ARRAY[i] + " created...");
        }

        contacts_list.getContacts(context);
        contacts_list.dbInsert(db);

        smsFromDeviceToDB(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Create tables
        for(int i=0; i<ARRAY_LENGTH; i++) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_ARRAY[i]);
            Log.d("Database Operations", "Table : " + TABLE_NAME_ARRAY[i] + " dropped...");
        }
        onCreate(db);
    }

    // MESSENGER_HISTORY data 추가 메소드
    public void insertMessengerHistory(int historyId, int contactId, String datetime, String day, String type, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("history_id", historyId);
        values.put("contact_id", contactId);
        values.put("datetime", datetime);
        values.put("day", day);
        values.put("type", type);
        values.put("count", count);
        db.insert("MESSENGER_HISTORY", null, values);
        Log.d("Database Operations", "Data inserted...");
        db.close();
    }

    /*
<<<<<<< HEAD
    Usage:
    DBHelper dbHelper = new DBHelper(context);
    dbHelper.insertCallLog(log_id, contact_id, datetime, name, phone, type, duration);
=======
    public void insertCallLog();
    Method for inserting a call log record
    This method takes the necessary parameters for a call log record (log_id, contact_id, datetime, name, phone, type, duration)
    and inserts them into the CALL_LOG table using the insert method of the SQLiteDatabase class.

    Usage:
    DBHelper dbHelper = new DBHelper(context);
    dbHelper.insertCallLog(log_id, contact_id, datetime, name, phone, type, duration);

    Note: log_id, contact_id, is the actual values that you want to insert
>>>>>>> origin/feature
     */
    public void insertCallLog(int log_id, int contact_id, long datetime, String name, String phone, int type, int duration) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBContract.CallLog.HISTORY_ID, log_id);
        values.put(DBContract.CallLog.KEY_CONTACT_ID, contact_id);
        values.put(DBContract.CallLog.DATETIME, datetime);
        values.put(DBContract.CallLog.NAME, name);
        values.put(DBContract.CallLog.PHONE, phone);
        values.put(DBContract.CallLog.TYPE, type);
        values.put(DBContract.CallLog.DURATION, duration);

        db.insert(DBContract.CallLog.TABLE_NAME, null, values);
        db.close();// constraints needed..?
    }

    public void smsFromDeviceToDB(SQLiteDatabase db) {
        int smsHistoryId = 0;   //기록 번호
        int smsContactId = 0;   //연락처 고유 아이디
        long smsDatetime = 0;    //연락 날짜
        String smsDay = "";     //연락 요일
        String smsType = "";    //연락 수단
        int smsCount = 0;       //일 연락 횟수(동일 연락 수단)

        Uri messagesUri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(messagesUri, null, null, null, null);

        if (cursor == null) Log.d("getSmsFromDevice", "cursor is null..");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                /* get sms from device */
                smsHistoryId = i++;
                int senderIndex = cursor.getColumnIndex("address");
                if (senderIndex >= 0) {
                    String smsSender = cursor.getString(senderIndex);
                }
                smsContactId = 1234;
                int dateIndex = cursor.getColumnIndex("date");
                if (dateIndex >= 0) {
                    smsDatetime = cursor.getLong(dateIndex);
                }
                smsDay = getDayOfDatetime(smsDatetime);
                //smsDay = "Monday"; //연락 요일
                smsType = "Sms"; //연락 수단
                smsCount = 0; //일 연락 횟수(동일 연락 수단)

                Log.d("getSmsFromDevice", "datetime : " + smsDatetime);
                Log.d("getSmsFromDevice", "day : " + smsDay);
                Log.d("getSmsFromDevice", "type : " + smsType);
                Log.d("getSmsFromDevice", "count : " + smsCount);


                /* insert sms to db */
                ContentValues values = new ContentValues();
                values.put("history_id", smsHistoryId);
                values.put("contact_id", smsContactId);
                values.put("datetime", smsDatetime);
                values.put("day", smsDay);
                values.put("type", smsType);
                values.put("count", smsCount);
                db.insert("MESSENGER_HISTORY", null, values);

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
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
}

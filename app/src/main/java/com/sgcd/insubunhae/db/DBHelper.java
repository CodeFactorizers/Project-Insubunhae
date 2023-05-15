package com.sgcd.insubunhae.db;

import static com.sgcd.insubunhae.db.DBContract.ARRAY_LENGTH;
import static com.sgcd.insubunhae.db.DBContract.SQL_CREATE_TABLE_ARRAY;
import static com.sgcd.insubunhae.db.DBContract.TABLE_NAME_ARRAY;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper  {

    public DBHelper(Context context) {
        super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
        Log.d("Database Operations", "Database created...");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create tables
        for(int i=0; i<ARRAY_LENGTH; i++) {
            db.execSQL(SQL_CREATE_TABLE_ARRAY[i]);
            Log.d("Database Operations", "Table : " + TABLE_NAME_ARRAY[i] + " created...");
        }
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
    Usage:
    DBHelper dbHelper = new DBHelper(context);
    dbHelper.insertCallLog(log_id, contact_id, datetime, name, phone, type, duration);
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
        db.close();// check needed..?
    }

}

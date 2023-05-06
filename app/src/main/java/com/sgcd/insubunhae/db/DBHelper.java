package com.sgcd.insubunhae.db;

import static com.sgcd.insubunhae.db.DBContract.ARRAY_LENGTH;
import static com.sgcd.insubunhae.db.DBContract.SQL_CREATE_TABLE_ARRAY;
import static com.sgcd.insubunhae.db.DBContract.TABLE_NAME_ARRAY;

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
}

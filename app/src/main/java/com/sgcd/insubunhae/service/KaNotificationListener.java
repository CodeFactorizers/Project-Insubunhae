package com.sgcd.insubunhae.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.DBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class KaNotificationListener extends NotificationListenerService {
    private static final String TAG = KaNotificationListener.class.getSimpleName();
    int kaHistoryId = -1;
    int i = -1;

    SQLiteDatabase idb = MainActivity.dbHelper.getWritableDatabase();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        Notification notification=sbn.getNotification();
        Bundle extras = notification.extras;
        String name=null;
        String text=null;

        if(!TextUtils.isEmpty(packageName) && packageName.equals("com.kakao.talk")) {
            //Toast.makeText(this, "Kakao Talk message read", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onNotificationPosted");

            if(sbn.getPackageName().equalsIgnoreCase("com.kakao.talk")) {
                name=extras.getString(Notification.EXTRA_TITLE);
                text=extras.getString(Notification.EXTRA_TEXT);
                Toast.makeText(this, "kakao>> " + "Title: " + name + ", Text: " + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "kakao>> " + "Title: " + name + ", Text: " + text);
            }

            if (name != null) {
                kaFromNotificationToDB(name);
                Log.d("Ka", "not null");
            } else {
                Log.d("Ka", "nulllllll");
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Toast.makeText(this, "KaNotificationListner 2", Toast.LENGTH_SHORT).show();
    }

    public void kaFromNotificationToDB(String name) {

        int kaContactId = -1;   //연락처 고유 아이디
        long kaDatetime = 0;    //연락 날짜
        String kaDay = "";     //연락 요일
        String kaType = "";    //연락 수단
        int kaCount = -1;       //일 연락 횟수(동일 연락 수단)

        //HistoryId --------------------------------------------------------------------------------
        kaHistoryId = i--;

        //ContactId --------------------------------------------------------------------------------
        kaContactId = MainActivity.dbHelper.getIdFromContactName(name);

        //DateTime ---------------------------------------------------------------------------------
        kaDatetime = System.currentTimeMillis();

        //Day --------------------------------------------------------------------------------------
        kaDay = MainActivity.dbHelper.getDayOfDatetime(kaDatetime);

        //Type -------------------------------------------------------------------------------------
        kaType = "KakaoTalk";

        //Count ------------------------------------------------------------------------------------
        List<String> solo_list = new ArrayList<>();
        solo_list = MainActivity.dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                "count", "(contact_id = " + kaContactId +
                " AND type = '" + kaType + "' AND datetime = " + kaDatetime + ")");
        if (solo_list.size() == 0) { //없어서 새로 추가
            kaCount = 1;

            /* insert kakao talk to db */
            ContentValues values = new ContentValues();
            values.put("history_id", kaHistoryId);
            values.put("contact_id", kaContactId);
            values.put("datetime", kaDatetime);
            values.put("day", kaDay);
            values.put("type", kaType);
            values.put("count", kaCount);
            idb.insert("MESSENGER_HISTORY", null, values);
            Log.d("Ka", "new success!");

        } else {
            //
            int new_count = Integer.parseInt(String.valueOf(solo_list)) + 1;

            String query = "UPDATE Messenger_History SET count = " + new_count + " WHERE contact_id = " + kaContactId + " AND type = '" + kaType + "' AND datetime = " + kaDatetime;
            idb.execSQL(query);
            Log.d("Ka", "old success!");
        }

        idb.close();
    }
}
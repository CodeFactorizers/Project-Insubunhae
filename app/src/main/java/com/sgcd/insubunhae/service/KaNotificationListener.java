package com.sgcd.insubunhae.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.StringTokenizer;

public class KaNotificationListener extends NotificationListenerService {
    private static final String TAG = KaNotificationListener.class.getSimpleName();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        Notification notification=sbn.getNotification();
        Bundle extras = notification.extras;
        String id=null;
        String text=null;

        if(!TextUtils.isEmpty(packageName) && packageName.equals("com.kakao.talk")) {
            Toast.makeText(this, "Kakao Talk message read", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onNotificationPosted");

            if(sbn.getPackageName().equalsIgnoreCase("com.kakao.talk")) {
                id=extras.getString(Notification.EXTRA_TITLE);
                text=extras.getString(Notification.EXTRA_TEXT);
                //Toast.makeText(this, "kakao>> " + "Title: " + id + ", Text: " + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "kakao>> " + "Title: " + id + ", Text: " + text);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Toast.makeText(this, "KaNotificationListner 2", Toast.LENGTH_SHORT).show();
    }

    public void kaFromNotificationToDB() {



        
    }
}
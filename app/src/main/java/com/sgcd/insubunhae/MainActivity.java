package com.sgcd.insubunhae;

// [통계] 미니 캘린더
import static android.content.ContentValues.TAG;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.content.pm.PackageManager;

import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.view.MenuInflater;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.sgcd.insubunhae.databinding.ActivityMainBinding;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // DB 관련
    DBHelper dbHelper;
    SQLiteDatabase idb = null;
    private Cursor dbCursor;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 3;

    private long lastRetrievalDate = 0L; // Store the timestamp of the last retrieval


    // [통계] 미니 캘린더 관련
    private CalendarView calendarView;
    
    // 연락처 연동
    private ContactsList contacts_list = new ContactsList();

    //PR test comment2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getPermission(); //sms 접근권한 받는 메소드(contacts, call log도 이 메소드 내에 추가하면 될듯!)

        // Create new helper
        dbHelper = new DBHelper(this);
        // Get the database. If it does not exist, this is where it will
        // also be created.
        idb = dbHelper.getWritableDatabase();

        /* MESSENGER_HISTORY data 추가
        dbHelper.insertMessengerHistory(1, 1000, "2022-01-01 10:30:00", "SAT", "msg", 10);
        dbHelper.insertMessengerHistory(2, 1000, "2022-01-01 10:40:00", "SAT", "katalk", 5);
        dbHelper.insertMessengerHistory(3, 1001, "2022-01-02 10:30:00", "SUN", "msg", 10);
        dbHelper.insertMessengerHistory(4, 1002, "2022-01-03 10:30:00", "MON", "msg", 10);
        dbHelper.insertMessengerHistory(5, 1002, "2022-01-03 10:30:00", "MON", "msg", 10);
        dbHelper.insertMessengerHistory(6, 1002, "2022-01-04 10:30:00", "TUE", "msg", 10);
        dbHelper.insertMessengerHistory(7, 1004, "2022-01-04 10:30:00", "TUE", "msg", 10);
        dbHelper.insertMessengerHistory(8, 1004, "2022-01-04 10:30:00", "TUE", "msg", 10);
        dbHelper.insertMessengerHistory(9, 1005, "2022-01-05 10:30:00", "WED", "msg", 10);
        dbHelper.insertMessengerHistory(10, 1005, "2022-01-05 10:31:00", "WED", "msg", 10);
        dbHelper.insertMessengerHistory(11, 1005, "2022-01-05 10:32:00", "WED", "msg", 10);
        dbHelper.insertMessengerHistory(12, 1005, "2022-01-05 10:33:00", "WED", "msg", 10);
        dbHelper.insertMessengerHistory(13, 1005, "2022-01-05 10:34:00", "WED", "msg", 10);
        */

        calendarView = findViewById(R.id.calendarView);
        paintMiniCalendar();

        //contacts_list.getContacts(getApplicationContext());
        //contacts_list.dbInsert(idb, dbHelper);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        // call Log retrive button, in home UI
        Button callLogRetrieveButton = findViewById(R.id.callLogRetrieveButton);
        callLogRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call log permission check again
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                    if (lastRetrievalDate == 0L) {
                        // First retrieval, fetch all call log
                        Toast.makeText(MainActivity.this, "Fetching All Call Log...", Toast.LENGTH_SHORT).show();
                        fetchAllCallLog();
                    } else {
                        Toast.makeText(MainActivity.this, "Fetching Additional Call Log...", Toast.LENGTH_SHORT).show();
                        // Fetch additional call log since the last retrieval
                        // fetchAdditionalCallLog();
                    }
                    // Call log permission not granted, request the permission
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_READ_CALL_LOG);
                }
            }
        });
    }

    // [통계] 미니 캘린더 구현
    public void paintMiniCalendar() {
        int calc_fam = 0; // 친밀도(계산값)
        int content_score = 1; // 최근 연락내용(점수 1~5점)
        int user_fam = 1; // 친밀도(유저 입력)
        int how_long_month = -1; // 알고 지낸 시간(월)
        int recent_days = -1; // 최근 연락일 ~ 현재(일)
        int recent_score = -1; // 최근 연락일(점수 1~5점)

        // DB에서 data 추출할 예정
        String recent_contact = "23-05-09 13:30:00";
        String first_contact = "23-05-08 13:30:00";

        // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        String currentTimestamp = dateFormat.format(calendar.getTime());
        Log.d("Calendar", "currentTimestamp : " + currentTimestamp);
        //-------------------------------------------------------------------------------*/

        // how_long_month, recent_days, recent_score 계산 --------------------------------*/
        try {
            Date date1 = dateFormat.parse(recent_contact);
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            how_long_month = (int) (milliseconds / (30 * 24 * 60 * 60 * 1000));
            Log.d("Calendar", "how_long_month : " + how_long_month);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Date date1 = dateFormat.parse(first_contact);
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            recent_days = (int) (milliseconds / (24 * 60 * 60 * 1000));
            Log.d("Calendar", "recent_days : " + recent_days);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recent_days >= 0 && recent_days <= 3) {
            recent_score = 5;
        }
        else if (recent_days >= 4 && recent_days <= 7) {
            recent_score = 4;
        }
        else if (recent_days >= 8 && recent_days <= 30) {
            recent_score = 3;
        }
        else if (recent_days >= 31 && recent_days <= 180) {
            recent_score = 2;
        }
        else if (recent_days >= 180) {
            recent_score = 1;
        }
        Log.d("Calendar", "recent_score : " + recent_score);
        //-------------------------------------------------------------------------------*/

    }

    // Inflating the menu items from the menu_items.xml file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    // Handling the click events of the menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switching on the item id of the menu item
        switch (item.getItemId()) {
            case R.id.menu_btn2:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_btn1:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /*
    Below: Permission Related Methods & Log Process Methods
     */
    private void getPermission() {
        Log.d("getPermission", "getPermission");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("getPermission", "in if");
        } else {
            showToast("Contacts permission already granted.");
            requestCallLogPermission();// If contacts permission is granted, request call log permission
        }
    }

    private void requestCallLogPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_READ_CALL_LOG);
        } else {
            showToast("Call log permission already granted.");
            requestSmsPermission();// If call log permission is granted, request SMS permission
        }
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
        } else {
            showToast("SMS permission already granted.");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Contacts permission granted.");
                // Permission granted for reading contacts
                // Add your desired action here

                // If contacts permission is granted, request call log permission
                requestCallLogPermission();
            } else {
                showToast("Contacts permission denied.");
                // some appropriate actions like re-request permission..?
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Call log permission granted.");// If call log permission is granted, request SMS permission
                requestSmsPermission();
            } else {
                showToast("Call log permission denied.");
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("SMS permission granted.");
            } else {
                showToast("SMS permission denied.");
            }
        }
    }

    private void processCallLog(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            //int nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
            int durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
            int typeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);

            while (cursor.moveToNext()) {
                //String Name = cursor.getString(nameColumnIndex);
                String phoneNumber = cursor.getString(numberColumnIndex);
                int callType = cursor.getInt(typeColumnIndex);
                int duration = cursor.getInt(durationColumnIndex);
                long dateInMillis = cursor.getLong(dateColumnIndex);
                String dateInString = getFormattedDateTime(dateInMillis);

                // Insert the call log into the database
                //databaseHelper.insertCallLog(dateInMillis, phoneNumber, callType, duration);

                // Lookup name based on phone number
                String contactDisplayName = getContactDisplayName(phoneNumber);
                if (!contactDisplayName.isEmpty()) {
                    // Log the retrieved call information
                    Log.d(TAG, "Datetime Format: "+ dateInMillis);
                    Log.d(TAG, "Name: " + contactDisplayName + ", Phone Number: " + phoneNumber);
                    Log.d(TAG, "Call Date: " + dateInString + ", Duration: " + duration + ", Call Type: " + callType);
                }
            }
            // Update the last retrieval date to the latest call log date
            if (cursor.moveToFirst()) {
                lastRetrievalDate = cursor.getLong(dateColumnIndex);
            }

        }
        cursor.close();// be careful, you need to close just once.
    }

    private void fetchAllCallLog() {
        String[] projection = {
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
        };

        String sortOrder = CallLog.Calls.DATE + " DESC";

        // execute the task on a background thread (not main(UI) thread)
        new Thread(() -> {
            Cursor cursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, projection, null, null, sortOrder
            );

            if (cursor != null) {
                processCallLog(cursor);
                //cursor.close();

                // Perform UI updates indicating the task is complete
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Call log retrieval complete", Toast.LENGTH_SHORT).show();
                    // You can customize the toast message or duration as per your needs.
                });
            } else {// Handle the case where the cursor is null
                // Perform UI updates indicating the task is complete
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to retrieve call log", Toast.LENGTH_SHORT).show();
                    // You can display an error message or customize it based on the error scenario.
                });
            }
        }).start();
    }

    private void fetchAdditionalCallLog() {
        // Fetch additional call  only if the app has been refreshed
        if (isAppRefreshed()) {
            // Fetch the additional call log
            String[] projection = {
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE
            };

            String selection = CallLog.Calls.DATE + " > ?";

            String[] selectionArgs = {String.valueOf(lastRetrievalDate)};
            String sortOrder = CallLog.Calls.DATE + " DESC";

            // Execute the task on a background thread
            new Thread(() -> {
                Cursor cursor = getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                );

                if (cursor != null) {
                    processCallLog(cursor);
                }

                runOnUiThread(() -> {
                    // Display a completion message or update the UI accordingly
                    Toast.makeText(MainActivity.this, "Call log fetched successfully", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    //for retrieving additional call log (not for now)
    @Override
    protected void onResume() {
        super.onResume();

        boolean needToFetchAdditionalCallLog = isAppRefreshed();

        if (needToFetchAdditionalCallLog) {
            fetchAdditionalCallLog();
        }
    }

    // Method to check if fetchAdditionalCallLog() is needed.
    private boolean isAppRefreshed() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        long lastRefreshTimestamp = preferences.getLong("last_refresh_timestamp", 0);
        long currentTimestamp = System.currentTimeMillis();
        long refreshInterval = 24 * 60 * 60 * 1000; // 24 hours

        boolean isRefreshed = currentTimestamp - lastRefreshTimestamp >= refreshInterval;

        if (isRefreshed) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("last_refresh_timestamp", currentTimestamp);
            editor.apply();
        }
        return isRefreshed;
    }

    @SuppressLint("Range")
    private String getContactDisplayName(String phoneNumber) {
        String contactName = "";
        Cursor contactCursor = null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            contactCursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (contactCursor != null && contactCursor.moveToFirst()) {
                contactName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contactCursor != null) {
                contactCursor.close();
            }
        }
        return contactName;
    }

    // some additional functions start
    private String getFormattedDateTime(long timestampInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestampInMillis);
        return sdf.format(date);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    // some additional functions end
}

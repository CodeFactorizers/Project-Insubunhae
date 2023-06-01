package com.sgcd.insubunhae;

import static android.content.ContentValues.TAG;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.Manifest;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

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
import androidx.fragment.app.FragmentManager;
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
import android.widget.Toast;

import com.sgcd.insubunhae.databinding.ActivityMainBinding;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.ui.contacts_viewer.FragmentContactsObjectViewer;

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

    // 연락처 연동
    private ContactsList contacts_list = new ContactsList();

    //PR test comment2

    //about fragment
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private FragmentContactsObjectViewer fragmentContactsObjectViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getPermission()) { //sms 접근권한 받는 메소드(contacts, call log도 이 메소드 내에 추가하면 될듯!)
            // Create new helper
            dbHelper = new DBHelper(this);
            // Get the database. If it does not exist, this is where it will also be created.
            idb = dbHelper.getWritableDatabase();
        }


        BottomNavigationView navView = findViewById(R.id.nav_view);

        //contacts viewer
        fragmentManager = getSupportFragmentManager();
        fragmentContactsObjectViewer = new FragmentContactsObjectViewer();
        final Bundle bundle = new Bundle();
        //bundle.putParcelable("contactsList", dbHelper.getContactsList());
        bundle.putParcelableArrayList("contactsList", dbHelper.getContactsList().getContactsList());
        fragmentContactsObjectViewer.setArguments(bundle);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

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
            case R.id.menu_btn1:
                transaction = fragmentManager.beginTransaction();
                //View view = getLayoutInflater().from(this).inflate(R.layout.activity_main, null);
                //int id = view.getId();
                transaction.replace(R.id.nav_host_fragment_activity_main, fragmentContactsObjectViewer).commitAllowingStateLoss();
                break;
            case R.id.menu_btn2:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /*
    Below: Permission Related Methods & Log Process Methods
     */
    private boolean getPermission() {
        Log.d("getPermission", "getPermission");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("getPermission", "in if");
            return false;
        } else {
            showToast("Contacts permission already granted.");
            requestCallLogPermission();// If contacts permission is granted, request call log permission
            return true;
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

                // Lookup contact name and contact id based on phone number
                ContactInfo contactInfo = getContactInfo(phoneNumber);
                String contactInfoName = contactInfo.getName();
                String contactInfoId = contactInfo.getId();

                if (!contactInfoName.isEmpty()) {
                    // Log the retrieved call information
                    Log.d(TAG, "Contact ID: " + contactInfoId + ", Name: " + contactInfoName + ", Phone Number: " + phoneNumber);
                    Log.d(TAG, "Datetime in Millis / in String): " + dateInMillis + " / " + dateInString + ", Duration: " + duration + ", Call Type: " + callType);
                }
                //}
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


    public ContactInfo getContactInfo(String phoneNumber) {
        ContactInfo contactInfo = new ContactInfo();
        Cursor contactCursor = null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            contactCursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (contactCursor != null && contactCursor.moveToFirst()) {
                int idIndex = contactCursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                int nameIndex = contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                if (idIndex >= 0) {
                    contactInfo.setId(contactCursor.getString(idIndex));
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
    // some additional functions start

    public static class ContactInfo {
        private String id;
        private String name;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

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

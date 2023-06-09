package com.sgcd.insubunhae;

import static android.content.ContentValues.TAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.ui.contacts_viewer.FragmentContactsEditor;
import com.sgcd.insubunhae.ui.contacts_viewer.FragmentContactsObjectViewer;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    // DB 관련
    public static DBHelper dbHelper;
    SQLiteDatabase idb = null;
    private Cursor dbCursor;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CALL_LOG = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 3;
    private static final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATION = 4;

    private long lastRetrievalDate = 0L; // Store the timestamp of the last retrieval

    //PR test comment2

    //about fragment
    private FragmentManager fragmentManager;
    public FragmentManager myGetFragmentManager(){
        return fragmentManager;
    }

    private FragmentTransaction fragmentTransaction;
    private FragmentContactsObjectViewer fragmentContactsObjectViewer;
    private FragmentContactsEditor fragmentContactsEditor;
    private ContactsList contactsList;
    public ContactsList getContactsList(){ return this.contactsList;}

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //resource failed to call close 해결 위한 로그 설정(다른 메모리 누수도 감지하는듯?)
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());

        if (getPermission()) { //sms 접근권한 받는 메소드(contacts, call log도 이 메소드 내에 추가하면 될듯!)
            // Create new helper
            dbHelper = new DBHelper(this);
            // Get the database. If it does not exist, this is where it will also be created.
            idb = dbHelper.getWritableDatabase();
            contactsList = dbHelper.getContactsList();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("0608", "main activity");
        BottomNavigationView navView = findViewById(R.id.nav_view);

        //contacts viewer
        fragmentManager = getSupportFragmentManager();
        fragmentContactsObjectViewer = new FragmentContactsObjectViewer();
        fragmentContactsEditor = new FragmentContactsEditor();
//        final Bundle bundle = new Bundle();
//        bundle.putParcelableArrayList("contactsList", dbHelper.getContactsList().getContactsList());
//        fragmentContactsObjectViewer.setArguments(bundle);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 친밀도 계산
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        calculateFamiliarity(db);
        //db.close();

    }

    //contacts viewer 뒤로가기 인터페이스
    public interface onBackPressedListener{
        public void onBackPressed();
    }
    //contacts viewer 뒤로가기
    @Override
    public void onBackPressed(){
        Log.d("MainActivity", "onBackPressed activity\n");
        List<Fragment>  fragmentList = fragmentManager.getFragments();
        for(Fragment fragment : fragmentList){
            if(fragment instanceof onBackPressedListener){
                ((onBackPressedListener)fragment).onBackPressed();
                return;
            }
        }
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
                final Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("contactsListToViewer", dbHelper.getContactsList().getContactsList());
                fragmentContactsObjectViewer.setArguments(bundle);
                fragmentTransaction = fragmentManager.beginTransaction();
                //View view = getLayoutInflater().from(this).inflate(R.layout.activity_main, null);
                //int id = view.getId();
                fragmentTransaction.replace(R.id.container, fragmentContactsObjectViewer).commitAllowingStateLoss();
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

    //    // 친밀도 계산 [SMS only]
    public static void calculateFamiliarity(SQLiteDatabase db) {
        // MAIN_CONTACTS 에서 contact_id 리스트 가져오기
        List<String> contact_id_list = new ArrayList<>();
        contact_id_list = dbHelper.getAttributeValueFromTable("MAIN_CONTACTS", "contact_id", "contact_id >= 0");
        List<Integer> contact_id_list_int = new ArrayList<>();
        for (String str : contact_id_list) {
            int number = Integer.parseInt(str);
            contact_id_list_int.add(number);
        }
        Log.d("CalFam", "contact_id_list_int : " + contact_id_list_int);

        //각 contact_id에 대하여, 친밀도(calc_fam) 계산
        for (Integer cur_contact_id : contact_id_list_int) {

            int calc_fam = 0; // 친밀도(계산값)
            int recent_content = 0; //
            int content_score = 1; // 최근 연락내용(점수 1~5점)
            int user_fam = cur_contact_id; // 친밀도(유저 입력)
            int how_long_month = -1; // 알고 지낸 시간(월)
            int recent_days = -1; // 최근 연락일 ~ 현재(일)
            int recent_score = -1; // 최근 연락일(점수 1~5점)

            // [DB에서 추출] MESSENGER_HISTORY의 datetime, count
            List<String> m_dt = new ArrayList<>();
            m_dt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                    "datetime", "contact_id = " + cur_contact_id);
            //Log.d("CalFam", "sms_datetime : " + m_dt);
            List<String> m_cnt = new ArrayList<>();
            m_cnt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                    "count", "contact_id = " + cur_contact_id);
            List<Integer> m_cnt_int = new ArrayList<>();
            for (String str : m_cnt) {
                int number = Integer.parseInt(str);
                m_cnt_int.add(number);
            }
            //Log.d("CalFam", "sms_cnt : " + m_cnt);

            // [DB에서 추출] recent_contact, first_contact
            Long recent_contact = dbHelper.getMaxOfAttribute("MESSENGER_HISTORY", "datetime", cur_contact_id);
            //Log.d("CalFam", "recent_contact : " + recent_contact);
            Date date_recent_contact = new Date(recent_contact);
            SimpleDateFormat dateFormat_recent_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            String timestamp_recent_contact = dateFormat_recent_contact.format(date_recent_contact);

            Long first_contact = dbHelper.getMinOfAttribute("MESSENGER_HISTORY", "datetime", cur_contact_id);
            //Log.d("CalFam", "first_contact : " + first_contact);
            Date date_first_contact = new Date(first_contact);
            SimpleDateFormat dateFormat_first_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            String timestamp_first_contact = dateFormat_first_contact.format(date_first_contact);

            // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
            Date date_current = new Date();

            SimpleDateFormat dateFormat_current = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date_current);

            String timestamp_current = dateFormat_current.format(calendar.getTime());
            //-------------------------------------------------------------------------------*/

            // how_long_month, recent_days, recent_score 계산 --------------------------------*/
            try {
                Date date1 = dateFormat_recent_contact.parse(timestamp_first_contact);
                Date date2 = dateFormat_current.parse(timestamp_current);

                double milliseconds = date2.getTime() - date1.getTime();

                how_long_month = (int) (Math.round(milliseconds / (30.0 * 24.0 * 60.0 * 60.0 * 1000.0)));
            } catch (
                    Exception e) {
                e.printStackTrace();
            }

            try {
                Date date1 = dateFormat_first_contact.parse(timestamp_recent_contact);
                Date date2 = dateFormat_current.parse(timestamp_current);

                long milliseconds = date2.getTime() - date1.getTime();

                recent_days = (int) (milliseconds / (24 * 60 * 60 * 1000));

            } catch (
                    Exception e) {
                e.printStackTrace();
            }

            if (recent_days >= 0 && recent_days <= 3) {
                recent_score = 5;
            } else if (recent_days >= 4 && recent_days <= 7) {
                recent_score = 4;
            } else if (recent_days >= 8 && recent_days <= 30) {
                recent_score = 3;
            } else if (recent_days >= 31 && recent_days <= 180) {
                recent_score = 2;
            } else if (recent_days >= 180) {
                recent_score = 1;
            }
            //-------------------------------------------------------------------------------*/

            // recent_content
            for (
                    int number : m_cnt_int) {
                recent_content += number;
            }
            if (recent_content == 0) {
                content_score = 1;
            } else if (recent_content >= 1 && recent_content <= 500) {
                content_score = 2;
            } else if (recent_content >= 501 && recent_content <= 1000) {
                content_score = 3;
            } else if (recent_content >= 1001 && recent_content <= 9999) {
                content_score = 4;
            } else if (recent_content >= 10000) {
                content_score = 5;
            }

            // Calculate
            calc_fam = content_score * user_fam * how_long_month * recent_score;

            // Familiarity Equation Final Check
            //Log.d("CalFam", "content_score : " + content_score); //최근 연락 내용
            //Log.d("CalFam", "user_fam : " + user_fam); //친밀도 (유저 입력)
            //Log.d("CalFam", "how_long_month : " + how_long_month); //알고 지낸 시간(월)
            //Log.d("CalFam", "recent_score : " + recent_score); //최근 연락일
            Log.d("CalFam", "contact_id : " + cur_contact_id + " || calc_fam : " + calc_fam); //친밀도(계산값)

            // [DB에 data 추가] cur_contact_id에 대해 user_fam, recent_contact, first_contact, calc_fam 값 추가
            ContentValues values = new ContentValues();
            values.put("contact_id", cur_contact_id);
            values.put("user_fam", user_fam);
            values.put("calc_fam", calc_fam);
            values.put("recent_contact", timestamp_recent_contact);
            values.put("first_contact", timestamp_first_contact);
            db.insert("ANALYSIS", null, values);


        }
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
            //showToast("Contacts permission already granted.");
            requestCallLogPermission();// If contacts permission is granted, request call log permission
            return true;
        }
    }

    private void requestCallLogPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, MY_PERMISSIONS_REQUEST_READ_CALL_LOG);
        } else {
            //showToast("Call log permission already granted.");
            requestSmsPermission();// If call log permission is granted, request SMS permission
        }
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
        } else {
            //showToast("SMS permission already granted.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, MY_PERMISSIONS_REQUEST_POST_NOTIFICATION);
        } else {
            //showToast("Notification permission already granted.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showToast("Contacts permission granted.");
                // Permission granted for reading contacts
                // Add your desired action here

                // If contacts permission is granted, request call log permission
                requestCallLogPermission();
            } else {
               //showToast("Contacts permission denied.");
                // some appropriate actions like re-request permission..?
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showToast("Call log permission granted.");// If call log permission is granted, request SMS permission
                requestSmsPermission();
            } else {
                //showToast("Call log permission denied.");
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showToast("SMS permission granted.");
            } else {
                //showToast("SMS permission denied.");
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
                    //Toast.makeText(MainActivity.this, "Call log retrieval complete", Toast.LENGTH_SHORT).show();
                    // You can customize the toast message or duration as per your needs.
                });
            } else {// Handle the case where the cursor is null
                // Perform UI updates indicating the task is complete
                runOnUiThread(() -> {
                    //Toast.makeText(MainActivity.this, "Failed to retrieve call log", Toast.LENGTH_SHORT).show();
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
                    //Toast.makeText(MainActivity.this, "Call log fetched successfully", Toast.LENGTH_SHORT).show();
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

    //연락처 뷰어의 편집버튼 눌렀을 때 편집창으로 넘어감
    public void toEditor(Fragment fragment, int idx){
        final Bundle bundle = new Bundle();
        Contact tmp = dbHelper.getContactsList().getContact(idx);
        bundle.putParcelableArrayList("contactsListToEditor", dbHelper.getContactsList().getContactsList());
        bundle.putInt("toEditorIdx", idx);
        fragmentContactsEditor.setArguments(bundle);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragmentContactsEditor).addToBackStack("editor").commit();
        //fragmentTransaction.add(fragment, "editor").commit();
    }
    // some additional functions end
}

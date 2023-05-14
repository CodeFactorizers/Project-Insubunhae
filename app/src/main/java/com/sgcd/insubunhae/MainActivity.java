package com.sgcd.insubunhae;

// [통계] 미니 캘린더
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import android.widget.CalendarView;

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

    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1029;

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

        contacts_list.getContacts(getApplicationContext());
        contacts_list.dbInsert(idb, dbHelper);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
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

    private void getPermission() {
        Log.d("getPermission","getPermission");
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("getPermission", "in if");
        }
    }
}
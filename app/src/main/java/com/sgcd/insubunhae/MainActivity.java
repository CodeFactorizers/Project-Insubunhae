package com.sgcd.insubunhae;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("getPermission", "in if");
        }

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
        }
    }
}
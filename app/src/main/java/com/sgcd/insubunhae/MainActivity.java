package com.sgcd.insubunhae;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.view.MenuInflater;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sgcd.insubunhae.databinding.ActivityMainBinding;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.db.ContactsList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1029;

    DBHelper dbHelper;
    SQLiteDatabase idb = null;
    private Cursor dbCursor;

    private ContactsList contacts_list = new ContactsList();

    //PR test comment2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getPermission();
        // Create new helper
        dbHelper = new DBHelper(this);
        // Get the database. If it does not exist, this is where it will
        // also be created.
        idb = dbHelper.getWritableDatabase();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    private void getPermission(){
        Log.d("getPermission", "enter");
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("getPermission", "in if");
        }
    }

}
package com.sgcd.insubunhae.ui.statistics;

import static java.lang.String.valueOf;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sgcd.insubunhae.db.DBHelper;

public class StatisticsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    static DBHelper dbHelper;
    private MutableLiveData<String> tmp_string1;
    private MutableLiveData<String> tmp_string2;
    private MutableLiveData<String> tmp_string3;

    public StatisticsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("통계 정보 화면");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void setText(String s) {
        mText.setValue(s);
        //mText.postValue(s);
    }

    public void setDBHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public LiveData<String> getFirstContact(int cur_contact_id) {
        String tmp = dbHelper.getTimestampFromANALYSIS("first_contact", cur_contact_id);
        Log.d("AddTable", "first contact : " + tmp);
        tmp_string1 = new MutableLiveData<>("첫 연락일 : " + tmp);

        return tmp_string1;
    }

    public LiveData<String> getRecentContact(int cur_contact_id) {
        String tmp = dbHelper.getTimestampFromANALYSIS("recent_contact", cur_contact_id);
        Log.d("AddTable", "recent contact : " + tmp);
        tmp_string2 = new MutableLiveData<>("최근 연락일 : " + tmp);

        return tmp_string2;
    }

    public LiveData<String> getFam(int cur_contact_id) {
        int tmp = dbHelper.getIntFromTable("ANALYSIS", "calc_fam", "contact_id = " + cur_contact_id);
        Log.d("AddTable", "fam : " + tmp);
        tmp_string3 = new MutableLiveData<>("친밀도 : " + String.valueOf(tmp) + "점");

        return tmp_string3;
    }

}
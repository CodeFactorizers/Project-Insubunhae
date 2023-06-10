package com.sgcd.insubunhae.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatisticsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

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
}
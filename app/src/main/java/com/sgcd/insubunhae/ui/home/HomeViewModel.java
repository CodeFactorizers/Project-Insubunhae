package com.sgcd.insubunhae.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("홈 화면(그룹 맵 혹은 다른 화면 구성)");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
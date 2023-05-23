package com.sgcd.insubunhae.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("환경설정 화면");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
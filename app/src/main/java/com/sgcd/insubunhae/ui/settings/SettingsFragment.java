package com.sgcd.insubunhae.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.sgcd.insubunhae.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);
    }
}
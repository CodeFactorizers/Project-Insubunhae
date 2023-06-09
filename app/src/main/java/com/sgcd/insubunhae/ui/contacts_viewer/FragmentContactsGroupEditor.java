package com.sgcd.insubunhae.ui.contacts_viewer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sgcd.insubunhae.MainActivity;

public class FragmentContactsGroupEditor extends Fragment implements MainActivity.onBackPressedListener{
    private MainActivity activity;
    private Context context;

    public static FragmentContactsEditor newInstance(){
        return new FragmentContactsEditor();
    }
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onBackPressed() {
        activity.myGetFragmentManager().popBackStack();
    }
}

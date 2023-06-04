package com.sgcd.insubunhae.ui.contacts_viewer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentContactsEditorBinding;
import com.sgcd.insubunhae.databinding.FragmentContactsObjectViewerBinding;
import com.sgcd.insubunhae.db.ContactsList;

public class FragmentContactsEditor extends Fragment implements MainActivity.onBackPressedListner{
    private FragmentContactsEditorBinding binding;
    private Context context;
    private ContactsList contacts_list;
    private MainActivity activity;

    public static FragmentContactsEditor newInstance(){
        return new FragmentContactsEditor();
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (MainActivity) getActivity();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts_editor, container,false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onBackPressed() {
        activity.myGetFragmentManager().beginTransaction().remove(this).commit();
    }

}

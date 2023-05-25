package com.sgcd.insubunhae.ui.contacts_viewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentContactsObjectViewerBinding;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;

import java.util.ArrayList;

public class FragmentContactsObjectViewer extends Fragment {
    private FragmentContactsObjectViewerBinding binding;
    private Context context;
    private ContactsList contacts_list;
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_contacts_object_viewer, container,false);
        View root = binding.getRoot();
        ArrayList<Contact> tmp_list = this.getArguments().getParcelableArrayList("contactsList");
        Contact tmp = tmp_list.get(0);
        binding.setName(tmp.getName());
        ArrayList<String> tmp_phone_num = tmp.getPhoneNumber();
        binding.setPhoneNumber1(tmp_phone_num.get(0));
        binding.setPhoneNumber2(tmp_phone_num.get(1));
        binding.setPhoneNumber3(tmp_phone_num.get(2));
        if(tmp.getGroupId().size() != 0){
            binding.setIsGrouped(1);
        }
        binding.setGroupCount(tmp.getGroupId().size());

        //return inflater.inflate(R.layout.fragment_contacts_object_viewer, container, false);
        return root;
    }
}

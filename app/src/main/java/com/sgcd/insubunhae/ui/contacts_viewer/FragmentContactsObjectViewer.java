package com.sgcd.insubunhae.ui.contacts_viewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentContactsObjectViewerBinding;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;

import java.util.ArrayList;
import java.util.List;

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
        ArrayList<String> db_phone_num = tmp.getPhoneNumber();
        ArrayList<String> tmp_phone_num = new ArrayList<String>();
        for(int i = 0; i < 3;i++){
                if(db_phone_num.size() > i && db_phone_num.get(i) != null){
                    tmp_phone_num.add(db_phone_num.get(i));
                }
                else{
                    tmp_phone_num.add(" ");
                }
        }
        binding.setPhoneNumber(tmp_phone_num);
        if(tmp.getGroupId().size() != 0){
            binding.setIsGrouped(Integer.toString(1));
        }
        binding.setGroupCount(Integer.toString(tmp.getGroupCount()));
        if(tmp.getAddress().size() > 0) binding.setAddress(tmp.getAddress().get(0));
        if(tmp.getAddress().size() > 1) binding.setAddress2(tmp.getAddress().get(1));
        if(tmp.getEmail().size() > 0) binding.setEmail(tmp.getEmail().get(0));
        if(tmp.getEmail().size() > 1)binding.setEmail2(tmp.getEmail().get(1));
        binding.setWork(tmp.getCompany());
        binding.setSNSID(tmp.getSnsId());

        //return inflater.inflate(R.layout.fragment_contacts_object_viewer, container, false);
        return root;
    }
}

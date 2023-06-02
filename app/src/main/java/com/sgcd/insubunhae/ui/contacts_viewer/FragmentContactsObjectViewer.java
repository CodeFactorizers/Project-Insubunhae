package com.sgcd.insubunhae.ui.contacts_viewer;

import static androidx.constraintlayout.widget.ConstraintSet.GONE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

        //data binding
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_contacts_object_viewer, container,false);
        View root = binding.getRoot();

        //dynamic add view
        ConstraintLayout constLayout = (ConstraintLayout) root.findViewById(R.id.contacts_viewer_constLayout);

        ArrayList<Contact> tmp_list = this.getArguments().getParcelableArrayList("contactsList");
        Contact tmp = tmp_list.get(0);
        binding.setName(tmp.getName());
        //전화번호
        ArrayList<String> db_phone_num = tmp.getPhoneNumber();
//        ArrayList<String> tmp_phone_num = new ArrayList<String>();
//        for(int i = 0; i < 3;i++){
//                if(db_phone_num.size() > i && db_phone_num.get(i) != null){
//                    tmp_phone_num.add(db_phone_num.get(i));
//                }
//                else{
//                    root.findViewById(R.id.contacts_viewer_phoneNumber1);
//                    tmp_phone_num.add(" ");
//                }
//        }
        if(db_phone_num.size() > 0 && db_phone_num.get(0) != null){
            binding.setPhoneNumber1(db_phone_num.get(0));
        }
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_phoneNumber1);
            tmpView.setVisibility(View.GONE);
        }
        if(db_phone_num.size() > 1 && db_phone_num.get(1) != null){
            binding.setPhoneNumber2(db_phone_num.get(1));
        }
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_phoneNumber2);
            tmpView.setVisibility(View.GONE);
        }
        if(db_phone_num.size() > 2 && db_phone_num.get(2) != null){
            binding.setPhoneNumber3(db_phone_num.get(2));
        }
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_phoneNumber3);
            tmpView.setVisibility(View.GONE);
        }
        if(tmp.getGroupId().size() != 0){
            binding.setIsGrouped(Integer.toString(1));
        }
        binding.setGroupCount(Integer.toString(tmp.getGroupCount()));
        //주소
        if((tmp.getAddress().size() > 0) && (tmp.getAddress().get(0) !=null)) binding.setAddress(tmp.getAddress().get(0));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_address1);
            tmpView.setVisibility(View.GONE);
        }
        if(tmp.getAddress().size() > 1 && (tmp.getAddress().get(0) !=null)) binding.setAddress2(tmp.getAddress().get(1));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_address2);
            tmpView.setVisibility(View.GONE);
        }
        //이메일
        if(tmp.getEmail().size() > 0&& (tmp.getEmail().get(0) !=null)) binding.setEmail(tmp.getEmail().get(0));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_email);
            tmpView.setVisibility(View.GONE);
        }
        if(tmp.getEmail().size() > 1&& (tmp.getEmail().get(0) !=null))binding.setEmail2(tmp.getEmail().get(1));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_sub_email);
            tmpView.setVisibility(View.GONE);
        }
        //직장
        if(tmp.getCompany() != null)binding.setWork(tmp.getCompany());
        else{
            root.findViewById(R.id.contacts_viewer_work).setVisibility(View.GONE);
        }
        //sns id
        if(tmp.getSnsId() != null) binding.setSNSID(tmp.getSnsId());
        else root.findViewById(R.id.contacts_viewer_sns_id).setVisibility(View.GONE);

        return root;
    }
}

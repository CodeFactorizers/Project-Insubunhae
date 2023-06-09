package com.sgcd.insubunhae.ui.contacts_viewer;

import static androidx.constraintlayout.widget.ConstraintSet.GONE;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentContactsObjectViewerBinding;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentContactsObjectViewer extends Fragment implements MainActivity.onBackPressedListener {
    private FragmentContactsObjectViewerBinding binding;
    private Context context;
    private MainActivity activity;
    private View root;
    private ContactsList contactsList;
    private ArrayList<Contact> contactsArrayList;

    public static FragmentContactsObjectViewer newInstance(){
        return new FragmentContactsObjectViewer();
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (MainActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //data binding
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_contacts_object_viewer, container,false);
        root = binding.getRoot();

        //편집 버튼
        Button btn_edit = root.findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                activity.toEditor(FragmentContactsEditor.newInstance(), 0);
            }
        });
        contactsList = activity.getContactsList();
        Map<String, Group> groupMap = contactsList.getGroupMap();
        //group
        ArrayList<String> groupList = new ArrayList<>();
        contactsArrayList = this.getArguments().getParcelableArrayList("contactsListToViewer");
        Contact tmp = contactsArrayList.get(0);
        for(String i : tmp.getGroupId()){
            Log.d("contacts viewer", "groupId "+i);
            groupList.add(groupMap.get(i).getGroupName());
        }
        Log.d("contacts viewer", "groupList : " + groupList);



        bindToView(tmp);

        return root;
    }

    @Override
    public void onBackPressed() {
        Log.d("contactsviewer", "onBackPressed fragment\n");
        activity.myGetFragmentManager().beginTransaction().remove(this).commit();
    }

    public void bindToView(Contact contact){
        //연락처 정보 출력

        binding.setName(contact.getName());
        //전화번호
        ArrayList<String> db_phone_num = contact.getPhoneNumber();
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
        if(contact.getGroupId().size() != 0){
            String groupStr = new String();
            groupStr += contact.getGroupName().get(0);
            for(int i = 1; i < contact.getGroupCount(); i++){
                groupStr += ", " + contact.getGroupName().get(i);
            }
            binding.setGroupList(groupStr);
        }
        else{
            View tmpView = root.findViewById((R.id.contacts_viewer_group_list));
            tmpView.setVisibility(View.GONE);
        }
        binding.setGroupCount(Integer.toString(contact.getGroupCount()));
        //주소
        if((contact.getAddress().size() > 0) && (contact.getAddress().get(0) !=null)) binding.setAddress(contact.getAddress().get(0));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_address1);
            tmpView.setVisibility(View.GONE);
        }
        if(contact.getAddress().size() > 1 && (contact.getAddress().get(0) !=null)) binding.setAddress2(contact.getAddress().get(1));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_address2);
            tmpView.setVisibility(View.GONE);
        }
        //이메일
        if(contact.getEmail().size() > 0&& (contact.getEmail().get(0) !=null)) binding.setEmail(contact.getEmail().get(0));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_email);
            tmpView.setVisibility(View.GONE);
        }
        if(contact.getEmail().size() > 1&& (contact.getEmail().get(0) !=null))binding.setEmail2(contact.getEmail().get(1));
        else{
            View tmpView = root.findViewById(R.id.contacts_viewer_sub_email);
            tmpView.setVisibility(View.GONE);
        }
        //직장
        if(contact.getCompany() != null)binding.setWork(contact.getCompany());
        else{
            root.findViewById(R.id.contacts_viewer_work).setVisibility(View.GONE);
        }
        //sns id
        if(contact.getSnsId() != null) binding.setSNSID(contact.getSnsId());
        else root.findViewById(R.id.contacts_viewer_sns_id).setVisibility(View.GONE);
    }
}

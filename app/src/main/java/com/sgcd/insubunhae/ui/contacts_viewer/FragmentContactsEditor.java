package com.sgcd.insubunhae.ui.contacts_viewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentContactsEditorBinding;
import com.sgcd.insubunhae.databinding.FragmentContactsObjectViewerBinding;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.DBHelper;

import java.util.ArrayList;

//public class FragmentContactsEditor extends Fragment implements MainActivity.onBackPressedListener{
public class FragmentContactsEditor extends Fragment{
    private FragmentContactsEditorBinding binding;
    private FragmentContactsEditor fragmentContactsEditor;
    private Context context;
    private Contact contacts;
    private MainActivity activity;
    private View root;
    private ContactsEditorViewModel model;
    private ArrayList<String> old_group_list;
    private final String TAG = "editor";


    public static FragmentContactsEditor newInstance(){
        return new FragmentContactsEditor();
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (MainActivity) getActivity();
        fragmentContactsEditor = this;
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        liveData.observe(this, new Observer<Contact>() {
//            @Override
//            public void onChanged(Contact contact) {
//                setContact(contact);
//            }
//        });
//        if(liveData.getValue() == null){
//
//        }
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //Parcels.unwrap(savedInstanceState.getParcelable("contactsToEditor");
        //ArrayList<Contact> contactsList = this.getArguments().getParcelableArrayList("contactsListToEditor");
        ArrayList<Contact> contactsList = activity.getContactsList().getContactsList();
        int idx = this.getArguments().getInt("toEditorIdx");
        contacts = contactsList.get(idx);
        old_group_list = contacts.getGroupId();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts_editor, container,false);
        model = new ViewModelProvider(this).get(ContactsEditorViewModel.class);
        root = binding.getRoot();


        Button btn_save = root.findViewById(R.id.btn_save);
//        Button btn_back = root.findViewById(R.id.btn_back);
        EditText editText = root.findViewById(R.id.contacts_editor_name);
        Contact tmp = contacts;
        tmp = activity.getContactsList().getContact(idx);

//        btn_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                onBackPressed();
//                NavHostFragment.findNavController(fragmentContactsEditor).navigateUp();
//            }
//        });
        btn_save.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                contacts.setName(binding.getName());
//                contacts.updatePhoneNumber(0, binding.getPhoneNumber1());
//                contacts.updatePhoneNumber(1, binding.getPhoneNumber2());
//                contacts.updatePhoneNumber(2, binding.getPhoneNumber3());
                contacts.clearPhoneNumber();
//                if( isValid(binding.getPhoneNumber1())){
//                    contacts.setPhoneNumber(binding.getPhoneNumber1());
//                }
//                if(isValid(binding.getPhoneNumber2())){
//                    contacts.setPhoneNumber(binding.getPhoneNumber2());
//                }
//                if(isValid(binding.getPhoneNumber3())){
//                    contacts.setPhoneNumber(binding.getPhoneNumber3());
//                }
                String phoneNumber1 = ((EditText)root.findViewById(R.id.contacts_editor_phoneNumber1)).getText().toString();
                if(isValid(phoneNumber1)){
                    contacts.setPhoneNumber(phoneNumber1);
                }
                String phoneNumber2 = ((EditText)root.findViewById(R.id.contacts_editor_phoneNumber2)).getText().toString();
                if(isValid(phoneNumber2)){
                    contacts.setPhoneNumber(phoneNumber2);
                }
                String phoneNumber3 = ((EditText)root.findViewById(R.id.contacts_editor_phoneNumber3)).getText().toString();
                if(isValid(phoneNumber3)){
                    contacts.setPhoneNumber(phoneNumber3);
                }
                contacts.clearAddress();
                if(isValid(binding.getAddress())){
                    contacts.setAddress(binding.getAddress());
                }
                if(isValid(binding.getAddress2())){
                    contacts.setAddress(binding.getAddress2());
                }
                contacts.clearEmail();
                if(isValid(binding.getEmail())){
                    contacts.setEmail(binding.getEmail());
                }
                if(isValid(binding.getEmail2())){
                    contacts.setEmail(binding.getEmail2());
                }
                contacts.setCompany(binding.getWork());
                contacts.setSnsId(binding.getSNSID());
                model.liveData.setValue(contacts);
                contacts.updateDb(activity.getSQLiteDatabase(), activity.getContactsList().getGroupMap(), old_group_list);

                Toast.makeText(context, binding.getName(), Toast.LENGTH_SHORT).show();
                Log.d("editor", "before call back");
                NavHostFragment.findNavController(fragmentContactsEditor).navigateUp();

//                onBackPressed();
            }
        });

        setContact(contacts);
        model.liveData.observe(activity, new Observer<Contact>() {
            @Override
            public void onChanged(Contact contact) {

                Log.d(TAG, "in livedata. onchanged");
                contacts.cpyContact(contact);
            }
        });
        if(model.liveData.getValue() == null){
            model.liveData.setValue(contacts);
        }

        return root;
    }

//    @Override
//    public void onBackPressed() {
//        Log.d("editor", "onBackPressed");
////        activity.myGetFragmentManager().popBackStack();
//    }

    private static boolean isValid(String str){
        boolean ret = str == null || str.isEmpty();
        return !ret;
    }

    private void setContact(Contact contact){
        Contact tmp = contacts;
        binding.setName(contact.getName());
        //전화번호
        ArrayList<String> db_phone_num = contact.getPhoneNumber();
        if(db_phone_num.size() > 0 && db_phone_num.get(0) != null){
            binding.setPhoneNumber1(db_phone_num.get(0));
        }
        else{
            binding.setPhoneNumber1("");
//            View tmpView = root.findViewById(R.id.contacts_editor_phoneNumber1);
//            tmpView.setVisibility(View.GONE);
        }
        if(db_phone_num.size() > 1 && db_phone_num.get(1) != null){
            binding.setPhoneNumber2(db_phone_num.get(1));
        }
        else{
            binding.setPhoneNumber2("");
            View tmpView = root.findViewById(R.id.contacts_editor_phoneNumber2);
            //tmpView.setVisibility(View.GONE);
        }
        if(db_phone_num.size() > 2 && db_phone_num.get(2) != null){
            binding.setPhoneNumber3(db_phone_num.get(2));
        }
        else{
            binding.setPhoneNumber3("");
            View tmpView = root.findViewById(R.id.contacts_editor_phoneNumber3);
            //tmpView.setVisibility(View.GONE);
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
            binding.setGroupList("");
            View tmpView = root.findViewById((R.id.contacts_editor_group_list));
            //tmpView.setVisibility(View.GONE);
        }
        //주소
        if((contact.getAddress().size() > 0) && (contact.getAddress().get(0) !=null)) binding.setAddress(contact.getAddress().get(0));
        else{
            binding.setAddress("");
            View tmpView = root.findViewById(R.id.contacts_editor_address1);
            //tmpView.setVisibility(View.GONE);
        }
        if(contact.getAddress().size() > 1 && (contact.getAddress().get(0) !=null)) binding.setAddress2(contact.getAddress().get(1));
        else{
            binding.setAddress2("");
            View tmpView = root.findViewById(R.id.contacts_editor_address2);
            //tmpView.setVisibility(View.GONE);
        }
        //이메일
        if(contact.getEmail().size() > 0 && (contact.getEmail().get(0) !=null)) binding.setEmail(contact.getEmail().get(0));
        else{
            binding.setEmail("");
            View tmpView = root.findViewById(R.id.contacts_editor_email);
            //tmpView.setVisibility(View.GONE);
        }
        if(contact.getEmail().size() > 1 && (contact.getEmail().get(0) !=null))binding.setEmail2(contact.getEmail().get(1));
        else{
            binding.setEmail2("");
            View tmpView = root.findViewById(R.id.contacts_editor_sub_email);
            //tmpView.setVisibility(View.GONE);
        }
        //직장
        if(contact.getCompany() != null)binding.setWork(contact.getCompany());
        else{
            binding.setWork("");
            root.findViewById(R.id.contacts_editor_work).setVisibility(View.GONE);
        }
        //sns id
        if(contact.getSnsId() != null) binding.setSNSID(contact.getSnsId());
        else root.findViewById(R.id.contacts_editor_sns_id).setVisibility(View.GONE);
    }


}
package com.sgcd.insubunhae.ui.contacts_viewer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sgcd.insubunhae.databinding.FragmentContactsEditorBinding;
import com.sgcd.insubunhae.db.Contact;

public class ContactsEditorViewModel extends ViewModel {
    public MutableLiveData<Contact> liveData= new MutableLiveData<>();
}

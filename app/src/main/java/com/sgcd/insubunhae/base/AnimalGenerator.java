package com.sgcd.insubunhae.base;

import android.database.sqlite.SQLiteDatabase;
import com.sgcd.insubunhae.db.Contact;

import com.sgcd.insubunhae.db.ContactsList;

import java.util.ArrayList;

public class Zoo extends ContactsList {
    //SQLiteDatabase db;
//    void getContactFromAppDB(SQLiteDatabase db){
//
//    }
//    public ArrayList<Contact>
//    getContactsList();
    public ArrayList<Contact> animals_list;
    public String CageId;
    private String CageName;
    public String name;
    private int count;
    private ArrayList<String> memberList = new ArrayList<>();

    Zoo(String Id, String CageName, String name){
        super.getContactsList() = animals_list;
        count = 0;



    }

    public void setGroupId(String groupId){ this.groupId = groupId;}
    public String getGroupId(){ return groupId;}
    public void setGroupName(String groupName){ this.groupName = groupName;}
    public String getGroupName(){ return this.groupName;}
    public void setCount(){}
    public int getCount(){ return this.count;}
    public void setMemberList(String id){ this.memberList.add(id);}
    public ArrayList<String> getMemberList(){ return this.memberList;}
    public int getMemberListSize(){ return this.memberList.size();}




//        StringBuilder modifiedName = new StringBuilder();
//        String[] words = name.split(" ");
//        for (String word : words) {
//            if (word.length() > 7) {
//                modifiedName.append(word.replace(' ', '\n')).append(" ");
//            } else {
//                modifiedName.append(word).append(" ");
//            }
//        }
//        this.name = modifiedName.toString().trim();
//
//

    @Override
    public String toString() {
        return "Zoo["+name+"]";
    }
}


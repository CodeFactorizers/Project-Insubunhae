package com.sgcd.insubunhae.db;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Contact implements Parcelable {
    private String id;
    private ArrayList<String> phoneNumber = new ArrayList<String>();
    private ArrayList<String> numberType = new ArrayList<String>();
    private String name;
    private String label;
    private ArrayList<String> email = new ArrayList<String>();
    private ArrayList<String> address = new ArrayList<String>();
    private ArrayList<String> addressType = new ArrayList<String>();
    private ArrayList<String> groupName = new ArrayList<>();
    private ArrayList<String> groupId = new ArrayList<String>();
    private int isGrouped;
    private int groupCount;
    private String company;
    private String department;
    private String title;
    private String snsId;
    private boolean isSelect;

    public void cpyContact(Contact contact){
        this.id = contact.getId();
        this.phoneNumber = (ArrayList<String>)contact.getPhoneNumber().clone();
        this.name = contact.getName();
        this.label = contact.getLabel();
        this.email = (ArrayList<String>)contact.getEmail().clone();
        this.address = (ArrayList<String>)contact.getAddress().clone();
        this.groupName = (ArrayList<String>)contact.getGroupName().clone();
        this.groupId = (ArrayList<String>)contact.getGroupId().clone();
        this.isGrouped = contact.getIsGrouped();
        this.groupCount = contact.getGroupCount();
        this.company = contact.getCompany();
        this.snsId = contact.getSnsId();
    }
    public void updateDb(SQLiteDatabase idb, Map<String, Group> group_map, ArrayList<String> old_group_list){
        //연락처 정보
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", this.name);
        for(int i = 0; i < this.phoneNumber.size(); i++){
            contentValues.put("phone_number" + Integer.toString(i+1), this.phoneNumber.get(i));
        }
        for(int i = 0; i < this.address.size(); i++){
            contentValues.put("address" + Integer.toString(i+1), this.address.get(i));
        }
        if(this.email.size() >= 1){
            contentValues.put("email", this.email.get(0));
        }
        if(this.email.size() >= 2){
            contentValues.put("sub_email", this.email.get(1));
        }
        contentValues.put("work", this.company);
        contentValues.put("sns_id", this.snsId);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String getTime = sdf.format(date);
        contentValues.put("updated_date", getTime);
        String whereClause = "contact_id = ?";
        String[] whereArgs = {this.id};
        idb.update("MAIN_CONTACTS", contentValues, whereClause, whereArgs);

        //group 정보
        idb.delete("GROUP_MEMBER", whereClause, whereArgs);
        if(!old_group_list.equals(this.groupId)){
            for(String oldGroupId : old_group_list){
                group_map.get(oldGroupId).getMemberList().remove(id);
            }
            for(String groupId:this.groupId){
                group_map.get(groupId).setMemberList(id);
            }
        }
        for(String groupId:this.groupId) {
            contentValues.clear();
            contentValues.put("contact_id", id);
            contentValues.put("group_id", groupId);
            idb.insert("GROUP_MEMBER", null, contentValues);
        }
    }

    public Contact() {
        isGrouped = 0;
        groupCount = 0;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        phoneNumber = in.createStringArrayList();
        numberType = in.createStringArrayList();
        name = in.readString();
        label = in.readString();
        email = in.createStringArrayList();
        address = in.createStringArrayList();
        addressType = in.createStringArrayList();
        groupName = in.createStringArrayList();
        groupId = in.createStringArrayList();
        company = in.readString();
        department = in.readString();
        title = in.readString();
        snsId = in.readString();
        isSelect = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeStringList(phoneNumber);
        dest.writeStringList(numberType);
        dest.writeString(name);
        dest.writeString(label);
        dest.writeStringList(email);
        dest.writeStringList(address);
        dest.writeStringList(addressType);
        dest.writeStringList(groupName);
        dest.writeStringList(groupId);
        dest.writeString(company);
        dest.writeString(department);
        dest.writeString(title);
        dest.writeString(snsId);
        dest.writeByte((byte) (isSelect ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setLabel(String label){
        this.label = label;
    }
    public String getLabel(){
        return this.label;
    }
    public void setPhoneNumber(String number){
        this.phoneNumber.add(number);
    }
    public void updatePhoneNumber(int idx, String str){ this.phoneNumber.set(idx, str);}
    public void clearPhoneNumber(){this.phoneNumber.clear();}
    public ArrayList<String> getPhoneNumber(){
        return phoneNumber;
    }

    public void setNumberType(String type){
        this.numberType.add(type);
    }
    public ArrayList<String> getNumberType(){
        return numberType;
    }
    public void setEmail(String email){
        this.email.add(email);
    }
    public ArrayList<String> getEmail(){ return this.email;}
    public void updateEmail(int idx, String str){ this.email.set(idx, str);}
    public void clearEmail(){this.email.clear();}
    public void setAddress(String address){
        this.address.add(address);
    }
    public ArrayList<String> getAddress(){ return this.address;}
    public void setAddressType(String addressType){
        this.addressType.add(addressType);
    }
    public ArrayList<String> getAddressType(){ return this.addressType;}
    public void updateAddress(int idx, String str){ this.address.set(idx, str);}
    public void clearAddress(){this.address.clear();}

    public void setGroupName(String groupName){
        this.groupName.add(groupName);
    }
    public ArrayList<String> getGroupName(){ return this.groupName; }
    //public String getOnlyGroupName(){ return this.groupName.get(0); }
    public void setGroupId(String groupId){this.groupId.add(groupId); }
    public ArrayList<String> getGroupId(){
        return this.groupId;
    }
    public void setIsGrouped(int isGrouped){ this.isGrouped = isGrouped;}
    public int getIsGrouped(){return this.isGrouped;}
    public void setGroupCount(int groupCount){ this.groupCount = groupCount;}
    public int getGroupCount(){return this.groupCount;}
    public void setCompany(String company) { this.company = company;}
    public String getCompany() { return this.company;}
    public void setDepartment(String department) { this.department = department;}
    public String getDepartment() { return this.department;}
    public void setTitle(String title) { this.title = title;}
    public String getTitle() { return this.title;}
    public void setSnsId(String snsId) { this.snsId = snsId;}
    public String getSnsId() { return this.snsId;}

}
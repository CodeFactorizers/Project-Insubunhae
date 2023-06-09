package com.sgcd.insubunhae.db;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Contact implements Parcelable {
    private String id;
    private ArrayList<String> phoneNumber = new ArrayList<String>();
    private ArrayList<String> numberType = new ArrayList<String>();
    private String name;
    private String label;
    private ArrayList<String> email = new ArrayList<String>();
    private ArrayList<String> address = new ArrayList<String>();
    private ArrayList<String> addressType = new ArrayList<String>();
    private String groupName;
    private ArrayList<String> groupId = new ArrayList<String>();
    private String company;
    private String department;
    private String title;
    private String snsId;
    private boolean isSelect;
    public Contact() {
    }

    @Override
    public int describeContents() {
        return 0;
    }
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
    public void setAddress(String address){
        this.address.add(address);
    }
    public ArrayList<String> getAddress(){ return this.address;}
    public void setAddressType(String addressType){
        this.addressType.add(addressType);
    }
    public ArrayList<String> getAddressType(){ return this.addressType;}

    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public String getGroupName(){ return this.groupName; }
    public void setGroupId(String groupId){this.groupId.add(groupId); }
    public ArrayList<String> getGroupId(){
        return this.groupId;
    }
    public void setCompany(String company) { this.company = company;}
    public String getCompany() { return this.company;}
    public void setDepartment(String department) { this.department = department;}
    public String getDepartment() { return this.department;}
    public void setTitle(String title) { this.title = title;}
    public String getTitle() { return this.title;}
    public void setSnsId(String snsId) { this.snsId = snsId;}
    public String getSnsId() { return this.snsId;}



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeByte(this.isSelect ? (byte) 1 : (byte) 0);
    }

    protected Contact(Parcel in) {
        this.name = in.readString();
        this.isSelect = in.readByte() != 0;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
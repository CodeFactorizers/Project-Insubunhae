package com.sgcd.insubunhae.db;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ContactsList implements Parcelable {
    private ArrayList<Contact> contacts_list = new ArrayList<Contact>();
    public ArrayList<Contact> getContactsList(){
        return contacts_list;
    }
    public Contact getContact(int idx){ return contacts_list.get(idx);}
    private Map<String, Group> group_map = new HashMap<String, Group>();
    public Map<String, Group> getGroupMap(){
        return group_map;
    }
    public ContactsList(){}

    protected ContactsList(Parcel in) {
        contacts_list = in.createTypedArrayList(Contact.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(contacts_list);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ContactsList> CREATOR = new Creator<ContactsList>() {
        @Override
        public ContactsList createFromParcel(Parcel in) {
            return new ContactsList(in);
        }

        @Override
        public ContactsList[] newArray(int size) {
            return new ContactsList[size];
        }
    };

    public void updateContacts(int idx, Contact contact){
        Contact tmp = this.contacts_list.get(idx);
        tmp.setName(contact.getName());
    }

    private void groupIdToName(Contact contact){
        ArrayList<String> groupId = contact.getGroupId();
        if(groupId.isEmpty()){}
        else{
            for(String id : groupId) {
                contact.setGroupName(group_map.get(id).getGroupName());
            }
        }

    }

    @SuppressLint("Range")
    public ArrayList<Contact> getContacts(Context context, DBHelper dbHelper, SQLiteDatabase db) {
        Log.d("getContacts", "enter getContacts -------------");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // group
        Cursor groupCursor = resolver.query(
                ContactsContract.Groups.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        while (groupCursor.moveToNext()) {
            String groupName = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups.TITLE));
            String groupId = groupCursor.getString(groupCursor.getColumnIndex(ContactsContract.Groups._ID));
            // 그룹 정보 처리 코드
            Group group = new Group();
            group.setGroupName(groupName);
            group.setGroupId(groupId);
            //Log.d("group", groupName);
            //Log.d("group", groupId);
            group_map.put(groupId, group);
        }
        groupCursor.close();

        //contact
        if (cursor != null) {
            //각 연락처
            while (cursor.moveToNext()) {
                Contact contact = new Contact();

                //id, name
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.setId(id);
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.setName(name);

                // phoneNumber
                // 타입 번호 알아낼 것
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + "=?", new String[]{id}, null);

                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String numberType = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        contact.setPhoneNumber(phoneNumber);
                        contact.setNumberType(numberType);
                    }
                    phoneCursor.close();
                }

                // phone lookup label
                // ContactsContract.FullNameStyle 로 이름이 무슨 언언지 확인. 1 : 영어, 5 : 한글


                //ContactsContract.PhoneLookup.LABEL
                // email
                Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

                while (emailCursor != null && emailCursor.moveToNext()) {
                    String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    contact.setEmail(email);
                }
                emailCursor.close();


                //address
                //if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.HAS_PHONE_NUMBER))) > 0) {
                Cursor addressCursor = resolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID + "=?", new String[]{id}, null);
                // 커스텀 타입 처리 해야 함
                while (addressCursor != null && addressCursor.moveToNext()) {
                    String address = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                    String addressType = addressCursor.getString(addressCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

                    contact.setAddress(address);
                    contact.setAddressType(addressType);
                    //Log.d("getContacts", "address : " + address + " type : " + addressType);
                }
                addressCursor.close();
                //}

                //groupMembership
                Uri groupMembershipUri = ContactsContract.Data.CONTENT_URI;
                String[] groupProjection = new String[] {ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID};
                String groupSelection = ContactsContract.Data.RAW_CONTACT_ID + " = ?" + " AND " +
                        ContactsContract.Data.MIMETYPE + " = ?";
                String[] groupSelectionArgs = new String[] {id, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE};
                Cursor groupMemberCursor = context.getContentResolver().query(groupMembershipUri, groupProjection, groupSelection, groupSelectionArgs, null);
                while (groupMemberCursor != null && groupMemberCursor.moveToNext()) {
                    String groupRowId = groupMemberCursor.getString(groupMemberCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                    contact.setGroupId(groupRowId);
                    group_map.get(groupRowId).setMemberList(id);
                }
                contact.setGroupCount(contact.getGroupId().size());
                if(contact.getGroupCount() != 0){
                    contact.setIsGrouped(1);
                }
                if (groupMemberCursor != null) {
                    groupMemberCursor.close();
                }

                // organization
                Cursor orgCursor = resolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[] {
                                ContactsContract.CommonDataKinds.Organization.COMPANY,
                                ContactsContract.CommonDataKinds.Organization.TITLE,
                                ContactsContract.CommonDataKinds.Organization.DEPARTMENT
                        },
                        ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] {
                                id,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                        },
                        null);
                if(orgCursor.moveToNext()) {
                    String company = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                    String title = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                    String department = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
                    contact.setCompany(company);
                    contact.setTitle(title);
                    contact.setDepartment(department);
                }
                orgCursor.close();
                contacts_list.add(contact);
            }

            cursor.close();
        }
        Log.d("getContacts", "exit getContacts -------------");

        return this.contacts_list;
    }

    public void getContactsFromAppDB(SQLiteDatabase db){
        Cursor groupInfoCursor = db.rawQuery("SELECT * FROM GROUP_INFO", null);
        while(groupInfoCursor.moveToNext()){
            Group group= new Group();
            String groupId = groupInfoCursor.getString(0);
            group.setGroupId(groupId);
            group.setGroupName(groupInfoCursor.getString(1));
            group_map.put(groupId,group);
        }
        groupInfoCursor.close();

        Cursor cursor = db.rawQuery("SELECT * FROM MAIN_CONTACTS", null);

        while(cursor.moveToNext()){
            Contact contact = new Contact();
            String id = cursor.getString(0);
            contact.setId(cursor.getString(0));
            contact.setName(cursor.getString(1));
            //if(!cursor.isNull(3))
                contact.setPhoneNumber(cursor.getString(3));
            //if(!cursor.isNull(5))
                contact.setPhoneNumber(cursor.getString(5));
            //if(!cursor.isNull(7))
                contact.setPhoneNumber(cursor.getString(7));
            contact.setIsGrouped(cursor.getInt(9));
            contact.setGroupCount(cursor.getInt(10));
            //if(!cursor.isNull(11))
                contact.setAddress(cursor.getString(11));
            //if(!cursor.isNull(13))
                contact.setAddress(cursor.getString(13));
            //if(!cursor.isNull(15))
                contact.setEmail(cursor.getString(15));
            //if(!cursor.isNull(16))
                contact.setEmail(cursor.getString(16));
            contact.setCompany(cursor.getString(17));
            contact.setSnsId(cursor.getString(18));
            //Log.d("getContactsFromAppDB", cursor.getInt(0) + " : " + cursor.getString(1) + ", " + cursor.getString(3));

            Cursor groupCursor = db.rawQuery("SELECT * FROM GROUP_MEMBER WHERE contact_id == " + id, null);
            while(groupCursor.moveToNext()){
                //Log.d("getdbfromapp", id + "가 속한 그룹 테이블 : " + groupCursor.getString(0) + " " + groupCursor.getString(1) + " " + groupCursor.getString((2)));
                contact.setGroupId(groupCursor.getString(2));
            }
            groupIdToName(contact);
            if(!contact.getGroupId().isEmpty()) {
                //Log.d("getdbfromapp", contact.getGroupName().toString());
                contact.setIsGrouped(1);
                contact.setGroupCount(contact.getGroupId().size());
            }
            groupCursor.close();

            contacts_list.add(contact);
        }
        cursor.close();

    }




    public void dbInsert(SQLiteDatabase db){
        db.beginTransaction();
        try{
            //Main_Contacts
            for(int i = 0; i < contacts_list.size(); i++) {
                Contact tmp = contacts_list.get(i);
                ContentValues cv = new ContentValues();
                cv.put("contact_id", tmp.getId());
                cv.put("name", tmp.getName());
                for (int j = 0; j < tmp.getPhoneNumber().size() && j < 3; j++) {
                    cv.put("phone_number" + Integer.toString(j + 1), tmp.getPhoneNumber().get(j));
                    cv.put("phone_number_type" + Integer.toString(j + 1), tmp.getNumberType().get(j));
                }
                if(tmp.getGroupId().size() == 0) {
                    cv.put("is_grouped", 0);
                }
                else{
                    cv.put("is_grouped", 1);
                }
                cv.put("group_count", tmp.getGroupId().size());
                for (int j = 0; j < tmp.getAddress().size() && j < 2; j++) {
                    cv.put("address" + Integer.toString(j + 1), tmp.getAddress().get(j));
                    cv.put("address_type" + Integer.toString(j + 1), tmp.getAddressType().get(j));
                }
                if (!tmp.getEmail().isEmpty()) {
                    cv.put("email", tmp.getEmail().get(0));
                    if (tmp.getEmail().size() > 1) {
                        cv.put("sub_email", tmp.getEmail().get(1));
                    }
                }

                if(tmp.getCompany() != null){
                    cv.put("work", tmp.getCompany());
                }

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String getTime = sdf.format(date);
                cv.put("updated_date", getTime);
                db.insert("Main_Contacts", null, cv);
            }

            //group
            for(Map.Entry<String, Group> entry : group_map.entrySet()){
                Group tmp = entry.getValue();
                for(int i = 0; i < tmp.getMemberListSize(); i++){
                    ContentValues cv = new ContentValues();
                    cv.put("group_id", entry.getKey());
                    cv.put("contact_id", tmp.getMemberList().get(i));
                    db.insert("Group_Member", null, cv);
                }
                ContentValues cv2 = new ContentValues();
                cv2.put("group_id", entry.getKey());
                cv2.put("group_name", tmp.getGroupName());
                cv2.put("member_count", tmp.getMemberListSize());
                db.insert("Group_Info", null, cv2);
            }
            db.setTransactionSuccessful();

        } finally{
            db.endTransaction();
        }
    }
}

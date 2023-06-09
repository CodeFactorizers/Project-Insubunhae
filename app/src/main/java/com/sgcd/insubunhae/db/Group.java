package com.sgcd.insubunhae.db;

import java.util.ArrayList;

public class Group {
    private String groupId;
    private String groupName;
    private int count;
    private ArrayList<String> memberList = new ArrayList<>();

    public Group(){
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

}

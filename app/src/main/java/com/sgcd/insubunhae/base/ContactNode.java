package com.sgcd.insubunhae.base;

import com.sgcd.insubunhae.R;

public class ContactNode {
    private String name;
    private String id;
    private int isGrouped;

    private int headId = R.drawable.baseline_person_outline_48;
    private int type;   //0이면 연락처. 1이면 그룹. 기본적으로 연락처

    public static final int CONTACT = 0;
    public static final int GROUP = 1;

    public ContactNode(){}
    public ContactNode(String name){
        this.name = name;
        this.type = GROUP;
    }
    public ContactNode(String id, String name, int isGrouped){
        this.type = CONTACT;
        this.id = id;

        StringBuilder modifiedName = new StringBuilder();
        String[] words = name.split(" ");
        for (String word : words) {
            if (word.length() > 7) {
                modifiedName.append(word.replace(' ', '\n')).append(" ");
            } else {
                modifiedName.append(word).append(" ");
            }
        }
        this.name = modifiedName.toString().trim();
        this.isGrouped = isGrouped;
    }

    public void setHeadId(int headId){this.headId = headId;}
    public int getContact(){return this.headId;}
    public void setType(int type){this.type = type;}
    public int getType(){return this.type;}
    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}
    public void setId(String id){this.id = id;}
    public String getId(){return this.id;}
    public void setIsGrouped(int isGrouped){this.isGrouped = isGrouped;}
    public int getIsGrouped(){return this.isGrouped;}
}

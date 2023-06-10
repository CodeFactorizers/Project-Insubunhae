package com.sgcd.insubunhae.base;

public class ContactNode {
    private String name;
    private String id;

    private int type;   //0이면 연락처. 1이면 그룹. 기본적으로 연락처
    private static final int CONTACT = 0;
    private static final int GROUP = 1;

    public ContactNode(){
        this.type = CONTACT;
    }
    public ContactNode(int type, String name){
        this.type = GROUP;
        this.name = name;}
}

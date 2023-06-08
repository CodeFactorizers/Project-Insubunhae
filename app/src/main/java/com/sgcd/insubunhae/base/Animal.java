package com.sgcd.insubunhae.base;

import com.sgcd.insubunhae.R;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  19:12
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * node bean
 */
public class Animal {
    public int headId = R.drawable.baseline_person_outline_48;
    public String name;
    public String groupName;
    public String phoneNumber;


    // 생성자1
    public Animal(String name) {
        this.name = name;
    }
    //생성자2
    public Animal(int headId, String name) {
        this.headId = headId;

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
    }

    public void setIcon(int id){
        this.headId = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public String getGroupName(){
        return groupName;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Animal["+name+"]";
    }
}


/*
public class Animal extends Fragment {


    public int icon = R.drawable.btn_radio_off_mtrl;
    public String name;
    public String groupName;
    public String phoneNumber;

    public Animal(String name) {
        this.name = name;
    }
    public Animal(int icon, String name){
        this.icon = icon;
        this.name = name;
    }

    public void setIcon(int id){
        this.icon = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public String getGroupName(){
        return groupName;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Animal[" + name + "]";
    }
}





 */

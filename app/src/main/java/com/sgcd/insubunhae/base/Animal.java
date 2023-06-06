package com.sgcd.insubunhae.base;

/**
 * @Author: 怪兽N
 * @Time: 2021/5/7  19:12
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * node bean
 */
public class Animal {
    public int headId;
    public String name;
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

    @Override
    public String toString() {
        return "Animal["+name+"]";
    }
}
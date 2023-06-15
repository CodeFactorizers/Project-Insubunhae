package com.sgcd.insubunhae.db;

public class Familiarity implements Comparable<Familiarity> {
    private Integer id;
    private String name;
    private int familiarity;

    public Familiarity(Integer id, String name, int familiarity){
        this.id = id;
        this.name = name;
        this.familiarity = familiarity;
    }

    public void setId(Integer id){ this.id = id;}
    public Integer getId(){return this.id;}
    public void setFamiliarity(int familiarity) {
        this.familiarity = familiarity;
    }
    public int getFamiliarity() {
        return familiarity;
    }

    @Override
    public int compareTo(Familiarity o) {
        if(o.familiarity < this.familiarity){
            return 1;
        } else if(o.familiarity > this.familiarity){
            return -1;
        }
        return 0;
    }

    public String toString(){
        return "[" + this.id + " : " + this.familiarity + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


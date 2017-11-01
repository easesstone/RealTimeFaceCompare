package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 性别：0->无；1->男；2->女；
 */
public enum Gender implements Serializable{
    None(0), Male(1), Female(2);

    private int gendervalue;

    private Gender(int gendervalue) {
        this.gendervalue = gendervalue;
    }

    public int getGendervalue() {
        return gendervalue;
    }

    public void setGendervalue(int gendervalue) {
        this.gendervalue = gendervalue;
    }

    public String toString() {
        return "Gendervalue{" + "value=" + gendervalue + '}';
    }
}

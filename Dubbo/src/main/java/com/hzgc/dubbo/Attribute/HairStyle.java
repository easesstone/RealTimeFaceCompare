package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 头发类型：0->无；1->直发；2->卷发；3->光头；
 */
public enum HairStyle implements Serializable{
    None(0), Straight(1), Wavy(2), Bald(3);

    private int hairstylevalue;

    private HairStyle(int hairstyle) {
        this.hairstylevalue = hairstyle;
    }

    public int getHairstyle() {
        return hairstylevalue;
    }

    public void setHairstyle(int hairstylevalue) {
        this.hairstylevalue = hairstylevalue;
    }

    public String toString() {
        return "HairStyle{" + "style=" + hairstylevalue + '}';
    }
}

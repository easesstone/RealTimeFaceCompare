package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否带帽子：0->无；1->戴帽子；2->没有戴帽子；
 */
public enum Hat implements Serializable{
    None(0), Hat_y(1), Hat_n(2);

    private int hatvalue;

    private Hat(int hatvalue) {
        this.hatvalue = hatvalue;
    }

    public int getHatvalue() {
        return hatvalue;
    }

    public void setHatvalue(int hatvalue) {
        this.hatvalue = hatvalue;
    }

    public String toString() {
        return "HatValue{" + "value=" + hatvalue + '}';
    }
}

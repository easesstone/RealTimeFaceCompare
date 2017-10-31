package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 胡子类型：0->无；1->鼻子和嘴唇之间的胡子;2->山羊胡；3->络腮胡；4->没有胡子；
 */
public enum Huzi implements Serializable{
    None(0), Mustache(1), Goatee(2), Sideburns(3), Nobeard(4);

    private int huzivalue;

    private Huzi(int huzivalue) {
        this.huzivalue = huzivalue;
    }

    public int getHuzivalue() {
        return huzivalue;
    }

    public void setHuzivalue(int huzivalue) {
        this.huzivalue = huzivalue;
    }

    public String toString() {
        return "HuziValue{" + "value=" + huzivalue + '}';
    }
}

package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 头发类型：0->无；1->直发；2->卷发；3->光头；
 */
public enum HairStyle implements Serializable {
    None(0), Straight(1), Wavy(2), Bald(3);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private HairStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Logistic getLogistic() {
        return logistic;
    }

    public void setLogistic(Logistic logistic) {
        this.logistic = logistic;
    }

    public static HairStyle get(int hairstylevalue) {
        for (HairStyle hairStyle : HairStyle.values()) {
            if (hairstylevalue == hairStyle.getValue()) {
                return hairStyle;
            }
        }
        return HairStyle.None;
    }
}

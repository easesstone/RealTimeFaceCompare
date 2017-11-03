package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 头发类型：0->无；1->直发；2->卷发；3->光头；
 */
public enum HairStyle implements Serializable {
    None, Straight, Wavy, Bald;

    private String value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Logistic getLogistic() {
        return logistic;
    }

    public void setLogistic(Logistic logistic) {
        this.logistic = logistic;
    }

    @Override
    public String toString() {
        return "Eyeglasses{" +
                "value='" + value + '\'' +
                ", logistic=" + logistic +
                '}';
    }
}

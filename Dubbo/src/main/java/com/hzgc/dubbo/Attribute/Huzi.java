package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 胡子类型：0->无；1->鼻子和嘴唇之间的胡子;2->山羊胡；3->络腮胡；4->没有胡子；
 */
public enum Huzi implements Serializable {
    None, Mustache, Goatee, Sideburns, Nobeard;

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

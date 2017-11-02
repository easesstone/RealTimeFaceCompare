package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 性别：0->无；1->男；2->女；
 */
public enum Gender implements Serializable{
    None, Male, Female;

    private String value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Iogistic logistic = Iogistic.OR;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Iogistic getLogistic() {
        return logistic;
    }

    public void setLogistic(Iogistic logistic) {
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

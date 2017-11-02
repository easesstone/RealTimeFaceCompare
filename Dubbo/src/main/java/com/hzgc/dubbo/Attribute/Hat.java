package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否带帽子：0->无；1->戴帽子；2->没有戴帽子；
 */
public enum Hat implements Serializable {
    None, Hat_y, Hat_n;

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

package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否系领带：0->无；1->系领带；2->没有系领带；
 */
public enum Tie implements Serializable {
    None, Tie_y, Tie_n;

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

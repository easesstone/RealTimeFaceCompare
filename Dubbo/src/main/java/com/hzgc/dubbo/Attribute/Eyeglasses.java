package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否戴眼镜：0->无；1->戴眼镜；2->没有戴眼镜；
 */
public enum Eyeglasses implements Serializable{
    None, Eyeglasses_y, Eyeglasses_n;

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

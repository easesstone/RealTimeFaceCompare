package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 胡子类型：0->无；1->鼻子和嘴唇之间的胡子;2->山羊胡；3->络腮胡；4->没有胡子；
 */
public enum Huzi implements Serializable {
    None(0), Mustache(1), Goatee(2), Sideburns(3), Nobeard(4);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Huzi(int value) {
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

    public static Huzi get(int huzivalue) {
        for (Huzi huzi : Huzi.values()) {
            if (huzivalue == huzi.getValue()) {
                return huzi;
            }
        }
        return Huzi.None;
    }
}

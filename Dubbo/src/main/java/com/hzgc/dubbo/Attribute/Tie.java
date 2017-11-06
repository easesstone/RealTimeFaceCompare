package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否系领带：0->无；1->系领带；2->没有系领带；
 */
public enum Tie implements Serializable {
    None(0), Tie_y(1), Tie_n(2);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Tie(int value) {
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

    public static Tie get(int tievalue) {
        for (Tie tie : Tie.values()) {
            if (tievalue == tie.getValue()) {
                return tie;
            }
        }
        return Tie.None;
    }
}

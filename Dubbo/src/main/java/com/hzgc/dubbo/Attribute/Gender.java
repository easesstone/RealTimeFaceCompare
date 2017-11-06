package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 性别：0->无；1->男；2->女；
 */
public enum Gender implements Serializable {
    None(0), Male(1), Female(2);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Gender(int value) {
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

    public static Gender get(int gendervalue) {
        for (Gender gender : Gender.values()) {
            if (gendervalue == gender.getValue()) {
                return gender;
            }
        }
        return Gender.None;
    }
}

package com.hzgc.dubbo.attribute;

import java.io.Serializable;

/**
 * 是否戴眼镜：0->无；1->戴眼镜；2->没有戴眼镜；
 */
public enum Eleglasses implements Serializable {
    None(0), Eyeglasses_y(1), Eyeglasses_n(2);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Eleglasses(int value) {
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

    public static Eleglasses get(int value) {
        for (Eleglasses eyeglasses : Eleglasses.values()) {
            if (value == eyeglasses.getValue()) {
                return eyeglasses;
            }
        }
        return Eleglasses.None;
    }

    /**
     * 获取属性描述
     *
     * @param eyeglasses 属性对象
     * @return 属性描述信息
     */
    public static String getDesc(Eleglasses eyeglasses) {
        if (eyeglasses == Eleglasses.None) {
            return "无";
        } else if (eyeglasses == Eleglasses.Eyeglasses_y) {
            return "戴眼镜";
        } else if (eyeglasses == Eleglasses.Eyeglasses_n) {
            return "没有戴眼镜";
        }
        return null;
    }
}

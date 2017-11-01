package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否戴眼镜：0->无；1->戴眼镜；2->没有戴眼镜；
 */
public enum Eyeglasses implements Serializable{
    None(0), Eyeglasses_y(1), Eyeglasses_n(2);

    private int eyeglassesvalue;

    private Eyeglasses(int eyeglassesvalue) {
        this.eyeglassesvalue = eyeglassesvalue;
    }

    public int getEyeglassesvalue() {
        return eyeglassesvalue;
    }

    public void setEyeglassesvalue(int eyeglassesvalue) {
        this.eyeglassesvalue = eyeglassesvalue;
    }

    public String toString() {
        return "EyeglassesValue{" + "value=" + eyeglassesvalue + '}';
    }
}

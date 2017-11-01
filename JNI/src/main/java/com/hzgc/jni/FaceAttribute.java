package com.hzgc.jni;

import com.hzgc.dubbo.Attribute.*;

import java.io.Serializable;
import java.util.Arrays;

public class FaceAttribute implements Serializable{
    /**
     * 特征值
     */
    private float[] feature;
    /**
     * 头发颜色
     */
    private HairColor hairColor;
    /**
     * 头发类型
     */
    private HairStyle hairStyle;
    /**
     * 性别
     */
    private Gender gender;
    /**
     * 是否带帽子
     */
    private Hat hat;
    /**
     * 是否系领带
     */
    private Tie tie;
    /**
     * 胡子类型
     */
    private Huzi huzi;
    /**
     * 是否戴眼镜
     */
    private Eyeglasses eyeglasses;

    public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }

    public HairColor getHairColor() {
        return hairColor;
    }

    public void setHairColor(HairColor hairColor) {
        this.hairColor = hairColor;
    }

    public HairStyle getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(HairStyle hairStyle) {
        this.hairStyle = hairStyle;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Hat getHat() {
        return hat;
    }

    public void setHat(Hat hat) {
        this.hat = hat;
    }

    public Tie getTie() {
        return tie;
    }

    public void setTie(Tie tie) {
        this.tie = tie;
    }

    public Huzi getHuzi() {
        return huzi;
    }

    public void setHuzi(Huzi huzi) {
        this.huzi = huzi;
    }

    public Eyeglasses getEyeglasses() {
        return eyeglasses;
    }

    public void setEyeglasses(Eyeglasses eyeglasses) {
        this.eyeglasses = eyeglasses;
    }

    @Override
    public String toString() {
        return "FaceAttr{" +
                "feature=" + Arrays.toString(feature) +
                ", hairColor=" + hairColor +
                ", hairStyle=" + hairStyle +
                ", gender=" + gender +
                ", hat=" + hat +
                ", tie=" + tie +
                ", huzi=" + huzi +
                ", eyeglasses=" + eyeglasses +
                '}';
    }
}

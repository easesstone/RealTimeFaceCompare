package com.hzgc.dubbo.Attribute;

import java.io.Serializable;
import java.util.Map;

/**
 * 属性统计
 */
public class AttributeCount implements Serializable {
    /**
     * 抓拍统计
     */
    private long captureCount;
    /**
     * 头发颜色属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> hairColorMap;
    /**
     * 头发类型属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> hairStyleMap;
    /**
     * 性别属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> genderMap;
    /**
     * 是否带帽子属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> hatMap;
    /**
     * 是否系领带属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> tieMap;
    /**
     * 胡子类型属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> huziMap;
    /**
     * 是否戴眼镜属性统计（Map<属性, 数量>）
     */
    private Map<Integer, Long> eyeglassesMap;

    public long getCaptureCount() {
        return captureCount;
    }

    public void setCaptureCount(long captureCount) {
        this.captureCount = captureCount;
    }

    public Map<Integer, Long> getHairColorMap() {
        return hairColorMap;
    }

    public void setHairColorMap(Map<Integer, Long> hairColorMap) {
        this.hairColorMap = hairColorMap;
    }

    public Map<Integer, Long> getHairStyleMap() {
        return hairStyleMap;
    }

    public void setHairStyleMap(Map<Integer, Long> hairStyleMap) {
        this.hairStyleMap = hairStyleMap;
    }

    public Map<Integer, Long> getGenderMap() {
        return genderMap;
    }

    public void setGenderMap(Map<Integer, Long> genderMap) {
        this.genderMap = genderMap;
    }

    public Map<Integer, Long> getHatMap() {
        return hatMap;
    }

    public void setHatMap(Map<Integer, Long> hatMap) {
        this.hatMap = hatMap;
    }

    public Map<Integer, Long> getTieMap() {
        return tieMap;
    }

    public void setTieMap(Map<Integer, Long> tieMap) {
        this.tieMap = tieMap;
    }

    public Map<Integer, Long> getHuziMap() {
        return huziMap;
    }

    public void setHuziMap(Map<Integer, Long> huziMap) {
        this.huziMap = huziMap;
    }

    public Map<Integer, Long> getEyeglassesMap() {
        return eyeglassesMap;
    }

    public void setEyeglassesMap(Map<Integer, Long> eyeglassesMap) {
        this.eyeglassesMap = eyeglassesMap;
    }

    @Override
    public String toString() {
        return "AttributeCount{" +
                "captureCount=" + captureCount +
                ", hairColorMap=" + hairColorMap +
                ", hairStyleMap=" + hairStyleMap +
                ", genderMap=" + genderMap +
                ", hatMap=" + hatMap +
                ", tieMap=" + tieMap +
                ", huziMap=" + huziMap +
                ", eyeglassesMap=" + eyeglassesMap +
                '}';
    }
}

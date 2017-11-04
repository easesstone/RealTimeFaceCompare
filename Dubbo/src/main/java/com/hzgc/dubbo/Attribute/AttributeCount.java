package com.hzgc.dubbo.Attribute;

import java.util.Map;

/**
 * 属性统计
 */
public class AttributeCount {
    /**
     * 抓拍统计
     */
    private long captureCount;
    /**
     * 属性统计 （Map<属性, 数量>）
     */
    private Map<String, Long> attributeMap;

    public long getCaptureCount() {
        return captureCount;
    }

    public void setCaptureCount(long captureCount) {
        this.captureCount = captureCount;
    }

    public Map<String, Long> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, Long> attributeMap) {
        this.attributeMap = attributeMap;
    }

    @Override
    public String toString() {
        return "AttributeCount{" +
                "captureCount=" + captureCount +
                ", attributeMap=" + attributeMap +
                '}';
    }
}

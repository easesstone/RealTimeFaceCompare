package com.hzgc.dubbo.Attribute;

import java.util.List;

/**
 * 属性统计
 */
public class AttributeCount {
    /**
     * 设备ID
     */
    private String IPCId;
    /**
     * 抓拍统计
     */
    private long captureCount;
    /**
     * 属性统计
     */
    private List<Attribute> attributes;

    public String getIPCId() {
        return IPCId;
    }

    public void setIPCId(String IPCId) {
        this.IPCId = IPCId;
    }

    public long getCaptureCount() {
        return captureCount;
    }

    public void setCaptureCount(long captureCount) {
        this.captureCount = captureCount;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "AttributeCount{" +
                "IPCId='" + IPCId + '\'' +
                ", captureCount=" + captureCount +
                ", attributes=" + attributes +
                '}';
    }
}

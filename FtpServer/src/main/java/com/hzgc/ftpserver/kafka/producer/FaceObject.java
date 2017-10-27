package com.hzgc.ftpserver.kafka.producer;

import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.jni.FaceAttr;

import java.io.Serializable;

/**
 * 人脸对象
 */
public class FaceObject implements Serializable{
    /**
     * 设备ID
     */
    private String ipcId;
    /**
     * 时间戳（格式：2017-01-01 00：00：00）
     */
    private String timeStamp;
    /**
     * 文件类型
     */
    private PictureType type;
    /**
     * 时间段（格式：0000）(小时+分钟)
     */
    private String timeSlot;
    /**
     * 人脸属性对象
     */
    private FaceAttr attribute;

    public String getIpcId() {
        return ipcId;
    }

    public void setIpcId(String ipcId) {
        this.ipcId = ipcId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public PictureType getType() {
        return type;
    }

    public void setType(PictureType type) {
        this.type = type;
    }

    public String getTimeQuantum() {
        return timeSlot;
    }

    public void setTimeQuantum(String timeQuantum) {
        this.timeSlot = timeQuantum;
    }

    public FaceAttr getAttribute() {
        return attribute;
    }

    public void setAttribute(FaceAttr attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return "FaceObject{" +
                "ipcId='" + ipcId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", type=" + type +
                ", timeSlot='" + timeSlot + '\'' +
                ", attribute=" + attribute +
                '}';
    }
}

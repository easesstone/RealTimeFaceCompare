package com.hzgc.collect.expand.reciver;

public class RecvicerEvent {
    /**
     * FTP url
     */
    private String url;

    /**
     * 当前数据序号
     */
    private long count;

    /**
     * 序列号
     */
    private long ipcID;

    /**
     * 接收时间戳
     */
    private String timeStamp;

    /**
     * 图片检测时间
     */
    private String date;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getIpcID() {
        return ipcID;
    }

    public void setIpcID(long ipcID) {
        this.ipcID = ipcID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

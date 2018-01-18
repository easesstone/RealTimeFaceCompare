package com.hzgc.collect.expand.log;

public class LogEvent {
    /**
     * FTP url
     */
    private String url;

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

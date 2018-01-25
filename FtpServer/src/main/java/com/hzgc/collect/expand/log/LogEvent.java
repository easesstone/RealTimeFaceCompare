package com.hzgc.collect.expand.log;

public class LogEvent {

    /**
     * 日志记数
     */
    private long count;

    /**
     * FTP url
     */
    private String url;

    /**
     * 接收时间戳
     */
    private String timeStamp;

    /**
     * 状态
     */
    private int status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

package com.hzgc.collect.expand.log;

public class LogEvent {

    /**
     * 当前LogEvent对应的序号,
     * 只在当前队列中保证有序自增长
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
     * 处理是否成功的标志
     * 0:在接收日志中为缺省值，在处理日志中为处理成功的标志
     * 1:只存在处理日志中，处理失败的标志
     */
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

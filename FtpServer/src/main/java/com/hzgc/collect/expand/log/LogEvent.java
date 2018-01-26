package com.hzgc.collect.expand.log;

public class LogEvent {

    /**
     * 当前LogEvent对应的序号,
     * 只在当前队列中保证有序自增长
     */
    private long count;

    /**
     * face path
     */
    private String path;

    /**
     * face absolute path
     */
    private String absolutePath;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
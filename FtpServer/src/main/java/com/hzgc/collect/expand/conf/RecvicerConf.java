package com.hzgc.collect.expand.conf;

public class RecvicerConf {
    /**
     * 接收队列日志名称
     */
    private String recviceLogName = "0000000000000000000";

    /**
     * 处理日志文件名称
     */
    private String processLogName = "0000000000000000000";

    /**
     * 当前队列序号
     */
    private long count;

    /**
     * 当前队列缓冲容量
     */
    private int capacity;

    /**
     * 接收队列日志目录
     */
    private String recviceLogDir;

    /**
     * 处理线程日志目录
     */
    private String processLogDir;

    /**
     * 当前队列ID
     */
    private String queueID;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getRecviceLogDir() {
        return recviceLogDir;
    }

    public void setRecviceLogDir(String recviceLogDir) {
        this.recviceLogDir = recviceLogDir;
    }

    public String getProcessLogDir() {
        return processLogDir;
    }

    public void setProcessLogDir(String processLogDir) {
        this.processLogDir = processLogDir;
    }

    public String getQueueID() {
        return queueID;
    }

    public void setQueueID(String queueID) {
        this.queueID = queueID;
    }
}

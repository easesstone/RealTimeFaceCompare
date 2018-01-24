package com.hzgc.collect.expand.conf;

public class CommonConf {

    /**
     * 接收队列日志名称
     */
    private String receiveLogName = "0000000000000000000";

    /**
     * 处理日志文件名称
     */
    private String processLogName = "0000000000000000000";

    /**
     * 接收队列日志文件大小
     */
    private int receiveLogSize = 300000;

    /**
     * 处理日志文件大小
     */
    private int processLogSize = 300000;

    /**
     * 当前队列缓冲容量
     */
    private int capacity;

    /**
     * 接收队列日志目录
     */
    private String receiveLogDir;

    /**
     * 处理队列日志目录
     */
    private String processLogDir;

    /**
     * 接收队列个数
     */
    private int receiveNumber;


    /**
     * 默认加载类路径下的cluster-over-ftp.properties文件
     */
    public CommonConf() {

    }

    /**
     *
     * @param properName 指定类路径下资源文件名称
     */
    public CommonConf(String properName) {

    }

    public String getReceiveLogName() {
        return receiveLogName;
    }

    public void setReceiveLogName(String receiveLogName) {
        this.receiveLogName = receiveLogName;
    }

    public String getProcessLogName() {
        return processLogName;
    }

    public void setProcessLogName(String processLogName) {
        this.processLogName = processLogName;
    }

    public int getReceiveLogSize() {
        return receiveLogSize;
    }

    public void setReceiveLogSize(int receiveLogSize) {
        this.receiveLogSize = receiveLogSize;
    }

    public int getProcessLogSize() {
        return processLogSize;
    }

    public void setProcessLogSize(int processLogSize) {
        this.processLogSize = processLogSize;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getReceiveLogDir() {
        return receiveLogDir;
    }

    public void setReceiveLogDir(String receiveLogDir) {
        this.receiveLogDir = receiveLogDir;
    }

    public String getProcessLogDir() {
        return processLogDir;
    }

    public void setProcessLogDir(String processLogDir) {
        this.processLogDir = processLogDir;
    }
}

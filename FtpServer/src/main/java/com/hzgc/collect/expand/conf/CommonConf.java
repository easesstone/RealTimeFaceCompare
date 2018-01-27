package com.hzgc.collect.expand.conf;

import com.hzgc.collect.expand.util.ClusterOverFtpProperHelper;
import com.hzgc.collect.expand.util.HelperFactory;

public class CommonConf {

    /**
     * 队列日志名称
     */
    private String logName = "0000000000000000000";

    /**
     * 日志文件大小
     */
    private int logSize = 300000;

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
        HelperFactory.regist();
        this.capacity = Integer.valueOf(ClusterOverFtpProperHelper.getCapacity());
        this.receiveLogDir = ClusterOverFtpProperHelper.getReceiveLogDir();
        this.processLogDir = ClusterOverFtpProperHelper.getProcessLogDir();
        this.receiveNumber = Integer.valueOf(ClusterOverFtpProperHelper.getReceiveNumber());

    }

    /**
     *
     * @param properName 指定类路径下资源文件名称
     */
    public CommonConf(String properName) {

    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public int getLogSize() {
        return logSize;
    }

    public void setLogSize(int logSize) {
        this.logSize = logSize;
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

    public int getReceiveNumber() {
        return receiveNumber;
    }

    public void setReceiveNumber(int receiveNumber) {
        this.receiveNumber = receiveNumber;
    }
}

package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

/**
 * 此对象为数据接收写日志对象的实例
 */
public class DataReceiveLogWriter extends AbstractLogWrite {
    /**
     * 接收队列日志文件大小
     */
    private int receiveLogSize;

    /**
     * 接收队列日志名称
     */
    private String receiveLogName;

    public DataReceiveLogWriter(CommonConf conf, String queueID) {
        super(conf, queueID);
    }

    /**
     * 接收队列日志目录
     */
    private String receiveLogDir;

    @Override
    public void writeEvent(LogEvent event) {
    }

    public int getReceiveLogSize() {
        return receiveLogSize;
    }

    public void setReceiveLogSize(int receiveLogSize) {
        this.receiveLogSize = receiveLogSize;
    }

    public String getReceiveLogName() {
        return receiveLogName;
    }

    public void setReceiveLogName(String receiveLogName) {
        this.receiveLogName = receiveLogName;
    }

    public String getReceiveLogDir() {
        return receiveLogDir;
    }

    public void setReceiveLogDir(String receiveLogDir) {
        this.receiveLogDir = receiveLogDir;
    }
}

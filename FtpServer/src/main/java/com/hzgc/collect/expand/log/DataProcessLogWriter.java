package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

/**
 * 此对象为数据处理写日志对象的实例
 */
public class DataProcessLogWriter extends AbstractLogWrite {
    /**
     * 处理日志文件大小
     */
    private int processLogSize;

    /**
     * 处理日志文件名称
     */
    private String processLogName;

    /**
     * 处理队列日志目录
     */
    private String processLogDir;

    /**
     * 当前队列序号
     */
    private long count;

    DataProcessLogWriter(CommonConf conf, String queueID, long count) {
        super(conf, queueID);
        this.count = count;

    }

    @Override
    public void writeEvent(LogEvent event) {
    }

    @Override
    protected void prepare() {

    }

    public int getProcessLogSize() {
        return processLogSize;
    }

    public void setProcessLogSize(int processLogSize) {
        this.processLogSize = processLogSize;
    }

    public String getProcessLogName() {
        return processLogName;
    }

    public void setProcessLogName(String processLogName) {
        this.processLogName = processLogName;
    }

    public String getProcessLogDir() {
        return processLogDir;
    }

    public void setProcessLogDir(String processLogDir) {
        this.processLogDir = processLogDir;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.RecvicerConf;

public abstract class AbstractLogGroupWrite implements LogWriter {
    /**
     * 日志文件名称
     */
    public String fileName;

    private AbstractLogGroupWrite() {

    }

    public AbstractLogGroupWrite(RecvicerConf conf) {

    }
}

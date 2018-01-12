package com.hzgc.collect.expand.log;

public abstract class AbstractLogGroupWrite implements LogGroupWriter {
    /**
     * 日志文件名称
     */
    public String fileName;

    /**
     * 被调度的日志名称
     */
    public String scheduledFileName;
}

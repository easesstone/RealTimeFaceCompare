package com.hzgc.collect.expand.log;

public interface LogGroupWriter {
    /**
     * 写日志方法
     *
     * @param event 日志信息
     * @return 是否写入成功
     */
    boolean writeEvent(String event);

    /**
     * 日志分组检查方法
     *
     */
    void logCheck();
}

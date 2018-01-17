package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.reciver.RecvicerEvent;

public interface LogWriter {

    /**
     * 写日志方法
     *
     * @param event 日志信息
     */
    void writeEvent(RecvicerEvent event);

}

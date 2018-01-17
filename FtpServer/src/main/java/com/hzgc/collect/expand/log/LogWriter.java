package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.reciver.RecvicerEvent;

/**
 * 此为数据接收和处理的公共接口，定义了写日志的方法，实现此接口的类具有如下功能：
 * 1.根据接收到的ReceiverEvent，将事件写入指定的日志文件中
 * 2.根据规定的日志文件大小进行日志分组
 * 3.使用顺序读写的方式进行日志文件的追加
 */
public interface LogWriter {

    /**
     * 写日志方法
     *
     * @param event 日志信息
     */
    void writeEvent(RecvicerEvent event);

}

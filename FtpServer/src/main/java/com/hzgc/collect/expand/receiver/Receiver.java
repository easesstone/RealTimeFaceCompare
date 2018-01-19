package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.log.LogEvent;

import java.util.concurrent.BlockingQueue;

public interface Receiver {
    /**
     * 此方法可将数据插入当前Recvicer的队列列
     *
     * @param data 数据对象
     */
    public void putData(LogEvent data);

    /**
     * 向RecvicerContainer注册Recvicer用来接收数据
     */
    public void registIntoContainer();

    /**
     * 开始处理数据
     */
    public void startProcess();

    /**
     * 获取当前队列
     */
    public BlockingQueue<LogEvent> getQueue();
}

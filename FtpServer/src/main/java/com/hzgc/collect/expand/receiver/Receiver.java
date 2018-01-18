package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.log.LogEvent;

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
     * 设置当前Recvicer中队列的序号
     *
     * @param count 当前队列序号
     */
    public void setCount(long count);

    /**
     * 开始处理数据
     */
    public void startProcess();
}

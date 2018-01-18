package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.log.LogWriter;

import java.util.concurrent.BlockingQueue;

public class ReceiverImpl implements Receiver {
    private CommonConf commonConf;
    private BlockingQueue<LogEvent> queue;
    private LogWriter writer;

    public ReceiverImpl() {
    }

    public ReceiverImpl(CommonConf commonConf) {
    }

    @Override
    public void putData(LogEvent event) {
    }

    @Override
    public void registIntoContainer() {
    }

    @Override
    public void setCount(long count) {

    }

    @Override
    public void startProcess() {

    }
}

package com.hzgc.collect.expand.reciver;

import com.hzgc.collect.expand.conf.RecvicerConf;
import com.hzgc.collect.expand.log.LogWriter;

import java.util.concurrent.BlockingQueue;

public class RecvicerImpl implements Recvicer {
    private RecvicerConf recvicerConf;
    private BlockingQueue<RecvicerEvent> queue;
    private LogWriter writer;

    public RecvicerImpl() {
    }

    public RecvicerImpl(RecvicerConf recvicerConf) {
    }

    @Override
    public void putData(RecvicerEvent event) {
        writer.writeEvent(event);
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

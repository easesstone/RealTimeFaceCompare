package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataReceiveLogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.log.LogWriter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ReceiverImpl implements Receiver {
    private BlockingQueue<LogEvent> queue;
    private LogWriter receiveWriter;
    private String queueID;

    private ReceiverImpl() {
    }

    public ReceiverImpl(CommonConf conf, String queueID) {
        this.queueID = queueID;
        this.queue = new ArrayBlockingQueue<>(conf.getCapacity());
        this.receiveWriter = new DataReceiveLogWriter(conf, queueID);
    }

    @Override
    public void putData(LogEvent event) {
        if (event != null) {
            try {
                queue.put(new LogEvent());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveWriter.countCheckAndWrite(event);
        }
    }

    @Override
    public void registerIntoContainer() {
    }

    @Override
    public void startProcess() {

    }

    @Override
    public BlockingQueue<LogEvent> getQueue() {
        return this.queue;
    }

    public String getQueueID() {
        return queueID;
    }

    public void setQueueID(String queueID) {
        this.queueID = queueID;
    }
}

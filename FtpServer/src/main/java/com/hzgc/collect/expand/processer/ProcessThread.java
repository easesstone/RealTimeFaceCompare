package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.RecvicerConf;
import com.hzgc.collect.expand.log.LogWriter;
import com.hzgc.collect.expand.reciver.RecvicerEvent;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private RecvicerConf conf;
    private BlockingQueue<RecvicerEvent> queue;
    private LogWriter writer;
    public ProcessThread(RecvicerConf conf, BlockingQueue<RecvicerEvent> queue, LogWriter writer) {
        this.conf = conf;
        this.queue = queue;
        this.writer = writer;
    }
    @Override
    public void run() {
    }

    public BlockingQueue<RecvicerEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<RecvicerEvent> queue) {
        this.queue = queue;
    }
}

package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogWriter;
import com.hzgc.collect.expand.log.LogEvent;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private CommonConf conf;
    private BlockingQueue<LogEvent> queue;
    private LogWriter writer;
    public ProcessThread(CommonConf conf, BlockingQueue<LogEvent> queue, LogWriter writer) {
        this.conf = conf;
        this.queue = queue;
        this.writer = writer;
    }
    @Override
    public void run() {
    }

    public BlockingQueue<LogEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<LogEvent> queue) {
        this.queue = queue;
    }
}

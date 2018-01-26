package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogWriter;
import com.hzgc.collect.expand.log.LogEvent;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private CommonConf conf;
    private BlockingQueue<LogEvent> queue;
    private LogWriter writer;
    public ProcessThread(CommonConf conf, BlockingQueue<LogEvent> queue, String queueID) {
        this.conf = conf;
        this.queue = queue;
        writer = new DataProcessLogWriter(this.conf, queueID);
    }

    @Override
    public void run() {
        while (true) {

        }
    }

    public BlockingQueue<LogEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<LogEvent> queue) {
        this.queue = queue;
    }
}

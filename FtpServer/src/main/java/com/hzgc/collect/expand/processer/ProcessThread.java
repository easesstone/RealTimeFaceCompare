package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.log.DataEvent;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private long count;
    private BlockingQueue<DataEvent> queue;
    public ProcessThread(long count, BlockingQueue<DataEvent> queue) {
        this.count = count;
        this.queue = queue;
    }
    @Override
    public void run() {

    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BlockingQueue<DataEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<DataEvent> queue) {
        this.queue = queue;
    }
}

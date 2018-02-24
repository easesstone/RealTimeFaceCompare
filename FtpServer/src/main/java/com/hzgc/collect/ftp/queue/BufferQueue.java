package com.hzgc.collect.ftp.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 缓冲队列(刘善彬 to 内)
 */
public class BufferQueue {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private static BufferQueue instance = null;

    private BufferQueue() {
    }

    public static BufferQueue getInstance() {
        if (instance == null){
            synchronized (BufferQueue.class){
                if (instance == null){
                    instance = new BufferQueue();
                }
            }
        }
        return instance;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

}

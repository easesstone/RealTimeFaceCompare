package com.hzgc.collect.expand.recive;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 缓冲队列(刘善彬 to 内)
 */
public class DataReciver {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private static DataReciver instance = null;

    private DataReciver() {
    }

    public static DataReciver getInstance() {
        if (instance == null){
            synchronized (DataReciver.class){
                if (instance == null){
                    instance = new DataReciver();
                }
            }
        }
        return instance;
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

}

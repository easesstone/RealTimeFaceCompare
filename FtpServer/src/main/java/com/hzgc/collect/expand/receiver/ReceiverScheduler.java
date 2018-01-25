package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverScheduler {
    private List<Receiver> container = new ArrayList<>();
    private CommonConf conf;

    private ExecutorService pool = Executors.newCachedThreadPool();

    private ReceiverScheduler() {

    }

    public ReceiverScheduler(CommonConf conf) {
        this.conf = conf;
    }

    /**
     * 向Recvicer对象即队列对象集合中的其中一个插入数据，采用轮询的方式,需要考虑线程安全
     *
     * @param event 封装的数据对象
     */
    public static void putData(LogEvent event) {
    }

    /**
     * 通过轮询的方式获取一个Recvicer对象
     *
     * @return 返回Recvicer对象
     */
    private static Receiver getRecvicer() {
        return null;
    }

    private void regist(Receiver receiver) {
        container.add(receiver);
    }

    public void preapreRecvicer() {

    }
}

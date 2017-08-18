package com.hzgc.rocketmq.util;

public class Test {
    public static void main(String[] args) {
        RocketMQProducer pp = RocketMQProducer.getInstance();
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
        pp.send("plat1", "bs2", "pp", "dd".getBytes(), null);
    }
}

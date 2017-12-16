package com.hzgc.ftpserver.util;

import com.codahale.metrics.*;

public class CounterTest {
    private static MetricRegistry metric = new MetricRegistry();
    /*
    记录执行次数
     */
    private final static Counter counter = metric.counter("counter");
    /*
     获取某个值   获取当前JVM可用内存
     */
    private final static Gauge<Long> gauge = metric.register("gauge", () -> Runtime.getRuntime().freeMemory());
    /*
    Meter用来计算事件的速率
     */
    private final static Meter meter = metric.meter("meter");

    /**
     * 使用metrics监测的方法
     */
    private static void test() {
        counter.inc();
        meter.mark();
    }

    public static void main(String[] args) {
        test();
        System.out.println("count          :" + counter.getCount());
        System.out.println("freeMemory     :" + gauge.getValue());
        System.out.println("count          :" + meter.getCount());
        System.out.println("mean rate      :" + meter.getMeanRate());
        System.out.println("1-minute rate  :" + meter.getOneMinuteRate());
        System.out.println("5-minute rate  :" + meter.getFiveMinuteRate());
        System.out.println("15-minute rate :" + meter.getFifteenMinuteRate());
    }
}

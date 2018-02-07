package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class ReceiverSchedulerTest {
    @Test
    public void preapreRecvicer() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setMergeScanTime(2);
        conf.setReceiveLogDir("/opt/test/receive");
        conf.setProcessLogDir("/opt/test/process");
        conf.setReceiveNumber(10);
        conf.setCapacity(100);
        conf.setLogName("00000000000000000.log");
        conf.setLogSize(100);
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        List<ReceiverImpl> list = receiverScheduler.getContainer();
        System.out.println(list.size());
        Assert.assertArrayEquals(new int[]{10}, new int[]{list.size()});
    }

    @Test
    public void getReceiver() throws Exception {
        boolean judge = false;
        CommonConf conf = new CommonConf();
        conf.setMergeScanTime(2);
        conf.setReceiveLogDir("/opt/test/receive");
        conf.setProcessLogDir("/opt/test/process");
        conf.setReceiveNumber(10);
        conf.setCapacity(100);
        conf.setLogName("00000000000000000.log");
        conf.setLogSize(100);
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        Method method = receiverScheduler.getClass().getDeclaredMethod("getRecvicer");
        method.setAccessible(true);
        Receiver receiver = (Receiver) method.invoke(receiverScheduler);
        if (receiver != null) {
            judge = true;
        }
        Assert.assertTrue(judge);
    }

    @Test
    public void putData() throws Exception {
        CommonConf conf = new CommonConf();
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        ThreadA threadA = new ThreadA(receiverScheduler);
        threadA.run();
        ThreadB threadB = new ThreadB(receiverScheduler);
        threadB.run();
    }


    @Test
    public void rebalanceReceiver() throws Exception {
        int receiverNum = 5;
        String processDir = "/opt/test/process";
        CommonConf conf = new CommonConf();
        conf.setMergeScanTime(2);
        conf.setReceiveLogDir("/opt/test/receive");
        conf.setProcessLogDir("/opt/test/process");
        conf.setReceiveNumber(10);
        conf.setCapacity(100);
        conf.setLogName("00000000000000000.log");
        conf.setLogSize(100);
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method = receiverScheduler.getClass().getDeclaredMethod("rebalanceReceiver", int.class, String.class);
        method.setAccessible(true);
        List<String> list = (List<String>) method.invoke(receiverScheduler, receiverNum, processDir);
        Assert.assertArrayEquals(new int[]{receiverNum}, new int[]{list.size()});
    }
}

class ThreadA implements Runnable {
    private ReceiverScheduler receiverScheduler;

    public ThreadA(ReceiverScheduler receiverScheduler) {
        super();
        this.receiverScheduler = receiverScheduler;
    }

    @Override
    public void run() {
        for (int i = 0; i < 30; i++) {
            try {
                LogEvent event = new LogEvent();
                event.setStatus("" + i);
                Method method = receiverScheduler.getClass().getDeclaredMethod("putData", LogEvent.class);
                method.setAccessible(true);
                method.invoke(receiverScheduler, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ThreadB implements Runnable {
    private ReceiverScheduler receiverScheduler;

    public ThreadB(ReceiverScheduler receiverScheduler) {
        super();
        this.receiverScheduler = receiverScheduler;
    }

    @Override
    public void run() {
        for (int i = 0; i < 20; i++) {
            try {
                LogEvent event = new LogEvent();
                event.setStatus("" + i);
                Method method = receiverScheduler.getClass().getDeclaredMethod("putData", LogEvent.class);
                method.setAccessible(true);
                method.invoke(receiverScheduler, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

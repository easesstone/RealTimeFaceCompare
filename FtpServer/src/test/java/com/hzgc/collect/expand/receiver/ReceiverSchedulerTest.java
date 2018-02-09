package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverSchedulerTest {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CommonConf conf = new CommonConf();
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        Thread threadA = new Thread(new PutDataThread(receiverScheduler,"threadA"));
        Thread threadB = new Thread(new PutDataThread(receiverScheduler,"threadB"));
        Thread threadC = new Thread(new PutDataThread(receiverScheduler,"threadC"));
        Thread threadD = new Thread(new PutDataThread(receiverScheduler,"threadD"));
        Thread threadE = new Thread(new PutDataThread(receiverScheduler,"threadE"));
        ExecutorService pool = Executors.newFixedThreadPool(5);
        pool.execute(threadA);
        pool.execute(threadB);
        pool.execute(threadC);
        pool.execute(threadD);
        pool.execute(threadE);
        pool.shutdown();
    }

    @Test
    public void preapreRecvicer() throws Exception {
        CommonConf conf = new CommonConf();
        int receiverNumber = conf.getReceiveNumber();
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        List<ReceiverImpl> list = receiverScheduler.getContainer();
        System.out.println(list.size());
        Assert.assertArrayEquals(new int[]{receiverNumber}, new int[]{list.size()});
    }

    @Test
    public void getReceiver() throws Exception {
        boolean judge = false;
        CommonConf conf = new CommonConf();
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
    public void rebalanceReceiver() throws Exception {
        int receiverNum = 5;
        CommonConf conf = new CommonConf();
        String processDir = conf.getProcessLogDir();
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method = receiverScheduler.getClass().getDeclaredMethod("rebalanceReceiver", int.class, String.class);
        method.setAccessible(true);
        List<String> list = (List<String>) method.invoke(receiverScheduler, receiverNum, processDir);
        Assert.assertArrayEquals(new int[]{receiverNum}, new int[]{list.size()});
    }
}

class PutDataThread extends Thread {
    private ReceiverScheduler receiverScheduler;
    private String thread;

    PutDataThread(ReceiverScheduler receiverScheduler, String thread) {
        super();
        this.receiverScheduler = receiverScheduler;
        this.thread = thread;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            LogEvent event = new LogEvent();
            event.setStatus(thread + i);
            event.setPath("ftp:s108:2181/2018/02/08");
            receiverScheduler.putData(event);
        }
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


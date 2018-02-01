package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Administrator on 2018-1-30.
 */
public class ReceiverSchedulerTest {
    @Test
    public void preapreRecvicer() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setReceiveLogDir("E:/getProcessLogDir/receive");
        conf.setProcessLogDir("E:/getProcessLogDir/process");
        conf.setReceiveNumber(10);
        conf.setCapacity(100);
        conf.setLogName("00000000000000000.log");
        conf.setLogSize(100);
        ReceiverScheduler receiverScheduler = new ReceiverScheduler(conf);
        Method method_prepareReceiver = receiverScheduler.getClass().getDeclaredMethod("preapreRecvicer");
        method_prepareReceiver.setAccessible(true);
        method_prepareReceiver.invoke(receiverScheduler);
        List<ReceiverImpl> list = receiverScheduler.getContainer();
        for (ReceiverImpl list1 : list) {
            String a = list1.getQueueID();
            System.out.println(a);
        }
    }

    @Test
    public void getContainer() throws Exception {
    }

    @Test
    public void putData() throws Exception {

    }

    @Test
    public void getPool() throws Exception {
    }

}
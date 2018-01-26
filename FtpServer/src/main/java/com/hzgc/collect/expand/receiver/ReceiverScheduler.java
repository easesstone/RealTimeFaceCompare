package com.hzgc.collect.expand.receiver;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.ProcessThread;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverScheduler {
    private Logger LOG = Logger.getLogger(ReceiverScheduler.class);
    private final List<ReceiverImpl> container = new ArrayList<>();
    private CommonConf conf;
    private int receiveNumber;
    private int pollingCount;
    private ExecutorService pool = Executors.newCachedThreadPool();

    public ReceiverScheduler(CommonConf conf) {
        this.conf = conf;
        this.receiveNumber = conf.getReceiveNumber();
        this.pollingCount = 0;
        this.preapreRecvicer();
    }

    /**
     * 将封装的LogEvent的日志对象调用recevier的putData方法，写入
     * receiver队列中
     *
     * @param event 封装的数据对象
     */

    public void putData(final LogEvent event) {
        synchronized (container) {
            getRecvicer().putData(event);
        }
    }

    /**
     * 采用依次取用receiver的方法，根据每次调用getReceiver方法
     * 对pollingCount进行自增，根据pollingCount和receiverNumber
     * 的余值判断使用哪个receiver
     *
     * @return 返回Recvicer对象
     */
    private Receiver getRecvicer() {
        Receiver receiver = container.get(this.pollingCount % this.receiveNumber);
        pollingCount++;
        return receiver;
    }

    /**
     * 将receiver注册至container容器中
     *
     * @param receiver 参数receiver，注册至容器中
     */
    private void regist(ReceiverImpl receiver) {
        container.add(receiver);
    }

    /**
     * 根据配置文件中配置的receiverNumber和日志文件地址调用rebanceReceiver方法
     * 取得对应的queueIdList，根据这些queueId去初始化receiver
     */
    private void preapreRecvicer() {
        List<String> queueIdList = reblanceRecevicer(conf.getReceiveNumber(), conf.getProcessLogDir());
        if (queueIdList.size() > 0) {
            for (String id : queueIdList) {
                ReceiverImpl receiver = new ReceiverImpl(conf, id);
                regist(receiver);
                pool.execute(new ProcessThread(conf, receiver.getQueue(), id));
            }
        } else {
            LOG.error("Can not to get the queueIdList,please check your ReceiverNumber and your FileDir!");
        }
    }

    /**
     * 根据参数receiverNum的数量和对应的日志存放数量，确定初始化receiver时，
     * receiver的数量和对应的queueId，返回对应queueId的集合
     *
     * @param receiverNum   参数receiver的数量
     * @param processLogDir 参数Log文件存放地址，用来判断receiver的数量
     * @return 返回经过判断的对应receiver数量的queueID的集合
     */
    private List<String> reblanceRecevicer(int receiverNum, String processLogDir) {
        List<String> queueIDList = new ArrayList<>();
        File file = new File(processLogDir);
        if (file.exists()) {
            String[] fileList = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.contains("-");
                }
            });
            if (fileList != null) {
                if (receiverNum == fileList.length) {
                    for (String fl : fileList) {
                        queueIDList.add(fl.split("-")[1]);
                    }
                } else if (receiverNum > fileList.length) {
                    for (int i = 0; i < receiverNum; i++) {
                        queueIDList.add(i + "");
                    }
                } else {
                    int[] queueIdArr = new int[fileList.length];
                    for (int i = 0; i < fileList.length; i++) {
                        queueIdArr[i] = Integer.parseInt(fileList[i].split("-")[1]);
                    }
                    Arrays.sort(queueIdArr);
                    for (int j = 0; j < receiverNum; j++) {
                        queueIDList.add(queueIdArr[j] + "");
                    }
                    return queueIDList;
                }

            } else {
                LOG.error("This file directory is null,please check your file dir!");
                return queueIDList;
            }
        } else {
            LOG.error("This file directory is not exist,please mkdir the file directory!");
            return queueIDList;
        }
        return queueIDList;
    }
}

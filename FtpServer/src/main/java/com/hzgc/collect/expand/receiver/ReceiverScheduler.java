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
     * 向Recvicer对象即队列对象集合中的其中一个插入数据,需要考虑线程安全
     * <p>
     * 获取到receiver之后存入数据，将数据存入到blockingQueue中，put
     *
     * @param event 封装的数据对象
     */

    public void putData(final LogEvent event) {
        synchronized (container) {
            getRecvicer().putData(event);
        }
    }

    /**
     * 通过轮询的方式获取一个Recvicer对象
     * <p>
     * 从List（container），采用轮询的方式，返回Receiver
     *
     * @return 返回Recvicer对象
     */
    private Receiver getRecvicer() {
        Receiver receiver = container.get(this.pollingCount % this.receiveNumber);
        pollingCount++;
        return receiver;
    }

    /**
     * 注册还包括指定queueID和日志路径，新建这个receiver的queueid和日志路径
     */
    private void regist(ReceiverImpl receiver) {
        container.add(receiver);
    }

    /**
     * 初始化Recevicer，其相对应的队列以及日志目录
     * 指定Recevicer----queueID、配置文件中含有的总日志路径
     * 再次启动时，判断是否存在Receiver
     * 接收路径示例：/opt/logdata/receive/r-queueid
     * 处理路径示例：/opt/logdata/process/p-queueid
     */
    private void preapreRecvicer() {
        //取得初始化Receiver的数量
        List<String> queueIdList = reblanceRecevicer(conf.getReceiveNumber(), conf.getProcessLogDir());
        if (queueIdList.size() > 0) {

            for (String id : queueIdList) {
                ReceiverImpl receiver = new ReceiverImpl(conf, id);
                regist(receiver);
                pool.execute(new ProcessThread(conf, receiver.getQueue(), id));
            }
        } else {
            LOG.error("");
        }
    }


    //遍历目录下的所有文件，判断和conf中指定的文件的大小,并调整
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
                return queueIDList;
            }
        } else {
            return queueIDList;
        }
        return queueIDList;
    }
}

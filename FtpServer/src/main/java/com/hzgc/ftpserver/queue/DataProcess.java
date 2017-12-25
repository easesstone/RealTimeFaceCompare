package com.hzgc.ftpserver.queue;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.ftpserver.common.FtpUtil;
import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.ftpserver.producer.ProducerOverFtp;
import com.hzgc.jni.FaceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列数据处理
 */
public class DataProcess {
    private static MetricRegistry metric = new MetricRegistry();
    private final static Counter counter = metric.counter("counter");

    public static void reader() {
        try {
            int threadNum = Integer.valueOf(QueueUtil.getProperties().getProperty("thread.number"));
            final Logger LOG = LoggerFactory.getLogger(DataProcess.class);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
            BlockingQueue queue = BufferQueue.getInstance().getQueue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ProducerOverFtp kafkaProducer = ProducerOverFtp.getInstance();
            for (int i = 0; i < threadNum; i++) {
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                String fileName = queue.take().toString();
                                int size = queue.size();
                                String ftpUrl = FtpUtil.filePath2absolutePath(fileName);
                                Map<String, String> map = FtpUtil.getFtpPathMessage(fileName);
                                String ipcID = map.get("ipcID");
                                String timeStamp = map.get("time");
                                String date = map.get("date");
                                String timeSlot = map.get("sj");
                                byte[] data = QueueUtil.getData(fileName);
                                FaceObject faceObject = new FaceObject();
                                faceObject.setIpcId(ipcID);
                                faceObject.setTimeStamp(timeStamp);
                                faceObject.setTimeSlot(timeSlot);
                                faceObject.setDate(date);
                                faceObject.setType(SearchType.PERSON);
                                if (data != null && data.length != 0) {
                                    FaceAttribute attribute = FaceFunction.featureExtract(data);
                                    float[] tz = attribute.getFeature();
                                    faceObject.setAttribute(attribute);
                                    faceObject.setStartTime(sdf.format(new Date()));
                                    kafkaProducer.sendKafkaMessage(ProducerOverFtp.getFEATURE(), ftpUrl, faceObject);
                                    counter.inc();
                                    LOG.info(Thread.currentThread() + "Send to kafka success,queue count:" + size + ";Process data count:" + counter.getCount());
                                } else {
                                    LOG.info(fileName + " data is null");
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

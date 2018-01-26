package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.ftp.util.FtpUtils;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.jni.FaceFunction;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private BlockingQueue<LogEvent> queue;
    private LogWriter writer;

    public ProcessThread(CommonConf conf, BlockingQueue<LogEvent> queue, String queueID) {
        this.queue = queue;
        writer = new DataProcessLogWriter(conf, queueID);
    }

    @Override
    public void run() {
        LogEvent event;
        KafkaProducer kafka = KafkaProducer.getInstance();
        try {
            while ((event = queue.take()) != null) {
                FaceAttribute attribute = FaceFunction.featureExtract(event.getAbsolutePath());
                FtpPathMessage message = FtpUtils.getFtpPathMessage(event.getPath());
                if (attribute.getFeature() != null) {
                    FaceObject faceObject = new FaceObject(message.getIpcid()
                            , message.getTimeStamp()
                            , SearchType.PERSON
                            , message.getDate()
                            , message.getTimeslot()
                            , attribute
                            , event.getTimeStamp());

                    kafka.sendKafkaMessage(KafkaProducer.getFEATURE()
                            , FtpUtils.getFtpUrl(event.getPath())
                            , faceObject);
                    writer.writeEvent(event);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

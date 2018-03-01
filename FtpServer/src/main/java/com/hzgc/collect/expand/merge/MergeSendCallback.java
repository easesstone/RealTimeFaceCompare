package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.log.LogEvent;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

public class MergeSendCallback implements Callback {
    private Logger LOG = Logger.getLogger(MergeSendCallback.class);
    private final long startTime = System.currentTimeMillis();
    private static MergeUtil mergeUtil = new MergeUtil();

    private String topic;
    private String key;
    private LogEvent event;
    private String writeErrFile;  //发送kafka错误日记
    private String processFile;   //写本地文件记录的日记

    MergeSendCallback(String topic, String key, LogEvent event) {
        this.topic = topic;
        this.key = key;
        this.event = event;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception e) {

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            event.setStatus("0");
            LOG.info("Send Kafka successfully! message:[topic:" + topic + ", key:" + key +
                    "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
        } else {
            event.setStatus("1");
            LOG.error("message:[" + key + "Send to Kafka failed! ");
            mergeUtil.writeMergeFile(event, writeErrFile);
            e.printStackTrace();
        }
        if (processFile != null) {
            mergeUtil.writeMergeFile(event, processFile);
        }
    }

    public String getWriteErrFile() {
        return writeErrFile;
    }

    public void setWriteErrFile(String writeErrFile) {
        this.writeErrFile = writeErrFile;
    }

    public String getProcessFile() {
        return processFile;
    }

    public void setProcessFile(String processFile) {
        this.processFile = processFile;
    }

    public LogEvent getEvent() {
        return event;
    }

    public void setEvent(LogEvent event) {
        this.event = event;
    }
}
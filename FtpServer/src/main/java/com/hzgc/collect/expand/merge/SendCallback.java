package com.hzgc.collect.expand.merge;

import com.codahale.metrics.Counter;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

public class SendCallback implements Callback {
    private Logger LOG = Logger.getLogger(SendCallback.class);
    final long startTime = System.currentTimeMillis();
    private Counter counter;
    String topic;
    String key;
    //发送kafka是否成功的标志
    boolean flag;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }


    SendCallback(String topic, String key) {
        this.topic = topic;
        this.key = key;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception e) {

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            setFlag(true);
            System.out.println("------------------------in this the boolean value is " + isFlag());
            LOG.info("Send Kafka successfully! message:[topic:" + topic + ", key:" + key +
                    "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
            counter.inc();
            LOG.info("Send Kafka total:" + counter.getCount());
        } else {
            setFlag(false);
            LOG.error("message:[" + key + "Send to Kafka failed! ");
            e.printStackTrace();
        }

    }
}
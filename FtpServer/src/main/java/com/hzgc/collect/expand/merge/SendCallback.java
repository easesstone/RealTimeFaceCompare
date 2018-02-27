package com.hzgc.collect.expand.merge;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

class SendCallback implements Callback {
    private Logger LOG = Logger.getLogger(SendCallback.class);
    final long startTime = System.currentTimeMillis();
    String topic;
    String key;
    boolean flag;

    public boolean getFlag() {
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
            LOG.info("Send Kafka successfully! message:[topic:" + topic + ", key:" + key +
                    "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
//            counter.inc();

//            LOG.info("Send Kafka total:" + counter.getCount());
        } else {
            setFlag(false);
            System.out.println("#########   flag=false  ############3");
            LOG.error("message:[" + key + "Send to Kafka failed! ");
            e.printStackTrace();
//            successToKafka = false;
        }

    }
}
package com.hzgc.collect.expand.merge;

import com.codahale.metrics.Counter;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import java.io.Serializable;

class SendDataToKafka extends KafkaProducer implements Serializable {
    private Logger LOG = Logger.getLogger(SendDataToKafka.class);
    private org.apache.kafka.clients.producer.KafkaProducer<String, FaceObject> kafkaProducer;
    private final Counter counter;
    private static SendDataToKafka sendDataToKafka;
    private boolean successToKafka;

    private SendDataToKafka() {
        super();
        kafkaProducer = KafkaProducer.kafkaProducer;
        counter = KafkaProducer.counter;
    }

    static synchronized SendDataToKafka getSendDataToKafka() {
        if (sendDataToKafka == null) {
            sendDataToKafka = new SendDataToKafka();
        }
        return sendDataToKafka;
    }


    class SendCallback implements Callback {
        final long startTime = System.currentTimeMillis();
        String topic;
        String key;

        SendCallback(String topic, String key) {
            this.topic = topic;
            this.key = key;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (e == null) {
                LOG.info("Send Kafka successfully! message:[topic:" + topic + ", key:" + key +
                        "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
                successToKafka = true;
                counter.inc();
                LOG.info("Send Kafka total:" + counter.getCount());
            } else {
                LOG.error("Send Kafka failed! message:[" + key + "]" + " send to partition(" + metadata.partition() + ")");
                e.printStackTrace();
                successToKafka = false;
            }
        }
    }

    @Override
    public void sendKafkaMessage(final String topic, final String key, FaceObject value) {

        kafkaProducer.send(new ProducerRecord<>(topic, key, value), new SendCallback(topic, key));

    }

    boolean isSuccessToKafka() {
        return successToKafka;
    }


}



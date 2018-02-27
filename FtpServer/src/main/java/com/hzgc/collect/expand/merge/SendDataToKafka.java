package com.hzgc.collect.expand.merge;

import com.codahale.metrics.Counter;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.Serializable;

class SendDataToKafka extends KafkaProducer implements Serializable {
    private Logger LOG = Logger.getLogger(SendDataToKafka.class);
    private org.apache.kafka.clients.producer.KafkaProducer<String, FaceObject> kafkaProducer;
    private final Counter counter;
    private static SendDataToKafka sendDataToKafka;

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

    public void sendKafkaMessage(final String topic, final String key, FaceObject value, SendCallback sendCallback ) {

        kafkaProducer.send(new ProducerRecord<>(topic, key, value), sendCallback);

    }
}



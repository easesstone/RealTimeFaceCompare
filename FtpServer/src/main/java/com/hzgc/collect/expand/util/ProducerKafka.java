package com.hzgc.collect.expand.util;


import com.hzgc.collect.expand.processer.FaceObject;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class ProducerKafka implements Serializable {
    private static Logger LOG = Logger.getLogger(ProducerKafka.class);
    public static ProducerKafka instance;
    private static KafkaProducer<String, FaceObject> kafkaProducer;

    private ProducerKafka() {
        kafkaProducer = new KafkaProducer<>(ProducerOverFtpProperHelper.getProps());
        LOG.info("Create ProducerKafka successfully!");
    }

    public void sendKafkaMessage(final String topic,
                                  final String key,
                                  final FaceObject value,
                                  final Callback callBack) {
        kafkaProducer.send(new ProducerRecord<>(topic, key, value), callBack);
    }

    public static ProducerKafka getInstance() {
        if (instance == null) {
            synchronized (ProducerKafka.class) {
                instance = new ProducerKafka();
            }
        }
        return instance;
    }

    public void closeProducer() {
        if (null != kafkaProducer) {
            kafkaProducer.close();
        }
    }
}


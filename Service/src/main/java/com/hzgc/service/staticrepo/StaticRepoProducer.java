package com.hzgc.service.staticrepo;

import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.util.ProducerKafka;
import com.hzgc.collect.expand.util.ProducerOverFtpProperHelper;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.util.Properties;

public class StaticRepoProducer {
    private static Logger LOG = Logger.getLogger(ProducerKafka.class);
    public static StaticRepoProducer instance;
    private static KafkaProducer<String, Object> kafkaProducer;
    private StaticRepoProducer() {
        Properties properties = ProducerOverFtpProperHelper.getProps();
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<>(properties);
        LOG.info("Create ProducerKafka successfully!");
    }

    public void sendKafkaMessage(final String topic, final String key, final String value) {
        LOG.info("Send kafka message [key:" + key + ", value:" + value + "]");
        kafkaProducer.send(new ProducerRecord<>(topic, key, value));
    }

    public static StaticRepoProducer getInstance() {
        if (instance == null) {
            synchronized (StaticRepoProducer.class) {
                instance = new StaticRepoProducer();
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

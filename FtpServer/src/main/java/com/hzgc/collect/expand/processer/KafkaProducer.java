package com.hzgc.collect.expand.processer;


import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.collect.expand.util.ProducerOverFtpProperHelper;
import com.hzgc.util.common.FileUtil;
import com.hzgc.collect.ftp.util.IOUtils;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

public class KafkaProducer implements Serializable {
    private static Logger LOG = Logger.getLogger(KafkaProducer.class);
    protected static org.apache.kafka.clients.producer.KafkaProducer<String, FaceObject> kafkaProducer;
    private Properties kafkaPropers = new Properties();
    private FileInputStream fis;
    private static String FEATURE = "feature";

    private static MetricRegistry metric = new MetricRegistry();
    protected final static Counter counter = metric.counter("sendKafkaCount");

    protected KafkaProducer() {
        try {
            kafkaPropers = ProducerOverFtpProperHelper.getProps();
            FEATURE = ProducerOverFtpProperHelper.getTopicFeature();
            if (kafkaPropers != null) {
                kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(kafkaPropers);
                LOG.info("Create KafkaProducer successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(fis);
        }
    }

    public void sendKafkaMessage(final String topic, final String key, FaceObject value) {
        final long startTime = System.currentTimeMillis();
        if (kafkaPropers != null) {
            kafkaProducer.send(new ProducerRecord<>(topic, key, value), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (metadata != null) {
                        LOG.info("Send Kafka successfully! message:[topic:" + topic + ", key:" + key +
                                "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
                        counter.inc();
                        LOG.info("Send Kafka total:" + counter.getCount());
                    } else {
                        LOG.error("Send Kafka failed! message:[" + key + "]" + " send to partition(" + metadata.partition() + ")");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void closeProducer() {
        if (null != kafkaProducer) {
            kafkaProducer.close();
        }
    }

    public static KafkaProducer getInstance() {
        return LazyHandler.instanc;
    }

    private static class LazyHandler {
        private static final KafkaProducer instanc = new KafkaProducer();
    }

    /*public static String getPicture() {
        return PICTURE;
    }

    public static String getFace() { return FACE; }

    public static String getJson() {
        return JSON;
    }*/

    public static String getFEATURE() {
        return FEATURE;
    }

}

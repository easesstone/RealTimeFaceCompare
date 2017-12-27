package com.hzgc.ftpserver.producer;

import com.hzgc.util.common.FileUtil;
import com.hzgc.util.common.IOUtil;
import com.hzgc.util.common.StringUtil;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class RocketMQProducer implements Serializable {
    private static Logger LOG = Logger.getLogger(RocketMQProducer.class);
    private static String topic;
    private static Properties properties = new Properties();
    private static RocketMQProducer instance = null;
    private DefaultMQProducer producer;

    private RocketMQProducer() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties"));
            properties.load(fis);
            String namesrvAddr = properties.getProperty("address");
            topic = properties.getProperty("topic");
            String producerGroup = properties.getProperty("group", UUID.randomUUID().toString());
            if (StringUtil.strIsRight(namesrvAddr) && StringUtil.strIsRight(topic) && StringUtil.strIsRight(producerGroup)) {
                producer = new DefaultMQProducer(producerGroup);
                producer.setNamesrvAddr(namesrvAddr);
                producer.start();
                LOG.info("producer started...");
            } else {
                LOG.error("parameter init error");
                throw new Exception("parameter init error");
            }
        } catch (Exception e) {
            LOG.error("producer init error...");
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeStream(fis);
        }
    }

    public static RocketMQProducer getInstance() {
        if (instance == null) {
            synchronized (RocketMQProducer.class) {
                if (instance == null) {
                    instance = new RocketMQProducer();
                }
            }
        }
        return instance;
    }

    public SendResult send(byte[] data) {
        return send(topic, null, null, data, null);
    }

    public SendResult send(String tag, byte[] data) {
        return send(topic, tag, null, data, null);
    }

    public SendResult send(String tag, String key, byte[] data) {
        return send(topic, tag, key, data, null);
    }

    public SendResult send(String topic, String tag, String key, byte[] data, final MessageQueueSelector selector) {
        SendResult sendResult = null;
        try {
            Message msg;
            if (tag == null || tag.length() == 0) {
                msg = new Message(topic, data);
            } else if (key == null || key.length() == 0) {
                msg = new Message(topic, tag, data);
            } else {
                msg = new Message(topic, tag, key, data);
            }
            LOG.info("Send MQ message[topic:" + msg.getTopic() + ", tag:" + msg.getTags() + ", key:" + msg.getKeys() + "]");
            //long startTime = System.currentTimeMillis();
            if (selector != null) {
                sendResult = producer.send(msg, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return selector.select(mqs, msg, arg);
                    }
                }, key);
            } else {
                sendResult = producer.send(msg);
            }
            //log.info(startTime);
            LOG.info(sendResult);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("send message error...");
        }
        return sendResult;
    }

    void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}

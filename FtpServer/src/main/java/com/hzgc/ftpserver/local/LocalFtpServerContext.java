package com.hzgc.ftpserver.local;


import com.hzgc.ftpserver.producer.ProducerOverFtp;
import com.hzgc.jni.NativeFunction;
import com.hzgc.rocketmq.util.RocketMQProducer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class LocalFtpServerContext extends DefaultFtpServerContext implements Serializable {
    static {
        NativeFunction.init();
    }

    private static Logger LOG = Logger.getLogger(LocalFtpServerContext.class);
    private ProducerOverFtp producerOverFtp = ProducerOverFtp.getInstance();
    private RocketMQProducer producerRocketMQ = RocketMQProducer.getInstance();

    ProducerOverFtp getProducerOverFtp() {
        return producerOverFtp;
    }

    RocketMQProducer getProducerRocketMQ() {
        return producerRocketMQ;
    }
}

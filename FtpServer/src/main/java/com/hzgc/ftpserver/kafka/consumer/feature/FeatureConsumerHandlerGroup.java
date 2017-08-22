package com.hzgc.ftpserver.kafka.consumer.feature;

import com.hzgc.ftpserver.kafka.consumer.ConsumerGroup;
import com.hzgc.ftpserver.kafka.consumer.ConsumerHandlerThread;
import com.hzgc.ftpserver.kafka.consumer.picture2.PicConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FeatureConsumerHandlerGroup implements ConsumerGroup, Serializable {
    private final Logger LOG = Logger.getLogger(FeatureConsumerHandlerGroup.class);
    private List<ConsumerHandlerThread> consumerHandler;
    private Connection hbaseConn;

    public FeatureConsumerHandlerGroup(Properties propers, Connection conn) {
        this.hbaseConn = conn;
        consumerHandler = new ArrayList<>();
        int consumerNum = Integer.parseInt(propers.getProperty("consumerNum"));
        LOG.info("The number of consumer thread is " + consumerNum);
        for (int i = 0; i < consumerNum; i++) {
            LOG.info("Start create the thread FeatureConsumerHandlerThread");
            ConsumerHandlerThread consumerThread = new FeatureConsumerHandlerThread(propers, hbaseConn, PicConsumerHandlerThread.class);
            consumerHandler.add(consumerThread);
        }
    }

    @Override
    public void execute() {
        for (ConsumerHandlerThread thread : consumerHandler) {
            LOG.info("Start-up the thread is FeatureConsumerHandlerThread");
            new Thread(thread).start();
        }
    }
}

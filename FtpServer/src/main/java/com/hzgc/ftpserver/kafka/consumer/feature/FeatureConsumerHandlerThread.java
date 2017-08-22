package com.hzgc.ftpserver.kafka.consumer.feature;

import com.hzgc.ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class FeatureConsumerHandlerThread extends ConsumerHandlerThread {
    public FeatureConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_feature");
        super.column_pic = propers.getProperty("c_feature_fea");
        super.column_ipcID = propers.getProperty("c_feature_ipcID");
        super.column_time = propers.getProperty("c_feature_time");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of FeatureConsumerHandlerThread success");
    }
}

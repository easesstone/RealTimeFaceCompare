package com.hzgc.ftpserver.kafka.consumer.feature;


import com.hzgc.ftpserver.kafka.consumer.ConsumerContext;
import com.hzgc.util.FileUtil;
import org.apache.hadoop.hbase.client.Connection;

import java.io.FileInputStream;

public class FeatureConsumerContext extends ConsumerContext {

    public FeatureConsumerContext(Connection conn) {
        super(conn);
    }

    @Override
    public void run() {
        try {
            resourceFile = FileUtil.loadResourceFile("consumer-feature.properties");
            System.out.println("****************************************************************************");
            propers.list(System.out);
            System.out.println("****************************************************************************");

            if (resourceFile != null) {
                propers.load(new FileInputStream(resourceFile));
            }
            FeatureConsumerHandlerGroup consumerGroup = new FeatureConsumerHandlerGroup(propers, conn);
            consumerGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

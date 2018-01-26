package util;

import com.hzgc.collect.expand.util.*;
import org.junit.Test;

import java.io.IOException;

public class ProperHelperTest {
	@Test
	public void clusterOverFtpProperHelperFun() throws IOException {

		HelperFactory.regist();
//		String Port = ClusterOverFtpProperHelper.getPort();
//        String implicitSsl = ClusterOverFtpProperHelper.getImplicitSsl();
//        String threadNum = ClusterOverFtpProperHelper.getThreadNum();
//
//		FTPAddressProperHelper.getUser();
//		FTPAddressProperHelper.getIp();
//		String pathRule = FTPAddressProperHelper.getPathRule();

		String bootstrapServers = ProducerOverFtpProperHelper.getBootstrapServers();
//		String clientId = ProducerOverFtpProperHelper.getClientId();
//        String topicFeature = ProducerOverFtpProperHelper.getTopicFeature();
//		String keySerializer = ProducerOverFtpProperHelper.getKeySerializer();
//		String requestRequiredAcks = ProducerOverFtpProperHelper.getRequestRequiredAcks();
//		String retries = ProducerOverFtpProperHelper.getRetries();
//		String valueSerializer = ProducerOverFtpProperHelper.getValueSerializer();
//
//		RocketMQProperHelper.getAddress();
//		RocketMQProperHelper.getTopic();
//		RocketMQProperHelper.getGroup();


	}
}

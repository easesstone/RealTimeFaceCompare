package util;

import com.hzgc.collect.expand.util.*;
import org.junit.Test;

import java.io.IOException;

public class ProperHelperTest {
	@Test
	public void clusterOverFtpProperHelperFun() throws IOException {

		HelperFactory.regist();
		ClusterOverFtpProperHelper.getPort();
		ClusterOverFtpProperHelper.getImplicitSsl();
		ClusterOverFtpProperHelper.getThreadNum();

		FTPAddressProperHelper.getUser();
		FTPAddressProperHelper.getIp();

		ProducerOverFtpProperHelper.getBootstrapServers();
		ProducerOverFtpProperHelper.getClientId();
		ProducerOverFtpProperHelper.getTopicFeature();
		ProducerOverFtpProperHelper.getKeySerializer();

		RocketMQProperHelper.getAddress();
		RocketMQProperHelper.getTopic();
		RocketMQProperHelper.getGroup();


	}
}

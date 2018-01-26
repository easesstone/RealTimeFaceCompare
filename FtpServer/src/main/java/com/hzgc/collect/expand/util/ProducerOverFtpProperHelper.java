package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 从配置文件producer-over-ftp.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class ProducerOverFtpProperHelper extends ProperHelper{
	private static Logger log = Logger.getLogger(ProducerOverFtpProperHelper.class);
    private static Properties props = new Properties();

	private static String bootstrapServers;
	private static String clientId;
	private static String requestRequiredAcks;
	private static String retries;
	private static String keySerializer;
	private static String valueSerializer;
	private static String topicFeature;

	static {
        String properName = "producer-over-ftp.properties";
        try {
			props.load(new FileInputStream(FileUtil.loadResourceFile(properName)));
			log.info("Load configuration for ftp server from ./conf/producer-over-ftp.properties");

			setBootstrapServers();
			setClientId();
			setRequestRequiredAcks();
			setRetries();
			setKeySerializer();
			setValueSerializer();
			setTopicFeature();

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Catch an unknown error, can't load the configuration file" + properName);
		}
	}

	/**
	 * set方法。验证配置文件中的值是否为符合条件的格式。
	 */
	private static void setBootstrapServers(){
        bootstrapServers = verifyIpPlusPortList("bootstrap.servers", props, log);
    }

	private static void setClientId(){
        clientId = verifyCommonValue("client.id", "p1", props, log);
    }

	private static void setRequestRequiredAcks(){
        requestRequiredAcks = verifyCommonValue("request.required.acks", "1", props, log);
    }

	private static void setRetries(){
        retries = verifyIntegerValue("retries", "0", props, log);
    }

	private static void setKeySerializer(){
        keySerializer = verifyCommonValue("key.serializer", "org.apache.kafka.common.serialization.StringSerializer", props, log);
    }

	private static void setValueSerializer(){
        valueSerializer = verifyCommonValue("value.serializer", "com.hzgc.ftpserver.producer.FaceObjectEncoder", props, log);
    }

	private static void setTopicFeature(){
        topicFeature = verifyCommonValue("topic-feature", "feature", props, log);
    }

	/**
	 * get方法。提供获取配置文件中的值的方法。
	 */
	public static String getBootstrapServers() {
		log.info("Load the configuration bootstrap.servers, the value is \"" + bootstrapServers + "\"");
		return bootstrapServers;
	}

	public static String getClientId() {
		log.info("Load the configuration client.id, the value is \"" + clientId + "\"");
		return clientId;
	}
	public static String getRequestRequiredAcks() {
		log.info("Load the configuration request.required.acks, the value is \"" + requestRequiredAcks + "\"");
		return requestRequiredAcks;
	}

	public static String getRetries(){
		log.info("Load the configuration retries, the value is \"" + retries + "\"");
		return retries;
	}

	public static String getKeySerializer(){
		log.info("Load the configuration key.serializer, the value is \"" + keySerializer + "\"");
		return keySerializer;
	}

	public static String getValueSerializer(){
		log.info("Load the configuration value.serializer, the value is \"" + valueSerializer + "\"");
		return valueSerializer;
	}

	public static String getTopicFeature(){
		log.info("Load the configuration topic-feature, the value is \"" + topicFeature + "\"");
		return topicFeature;
	}

}


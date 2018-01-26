package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 从配置文件rocketmq.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class RocketMQProperHelper extends ProperHelper{
	private static Logger log = Logger.getLogger(RocketMQProperHelper.class);
    private static Properties props = new Properties();
	private static String address;
	private static String topic;
	private static String group;

	static {
        String properName = "rocketmq.properties";
        try {
			props.load(new FileInputStream(FileUtil.loadResourceFile(properName)));
			log.info("Load configuration for ftp server from ./conf/rocketmq.properties");

			setAddress();
			setTopic();
			setGroup();

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Catch an unknown error, can't load the configuration file" + properName);
		}
	}

	/**
	 * set方法。验证配置文件中的值是否为符合条件的格式。
	 */
	private static void setAddress(){
        address = verifyIpPlusPort("address", props, log);
    }

	private static void setTopic(){
        topic = verifyCommonValue("topic", "REALTIME_PIC_MESSAGE", props, log);
    }

	private static void setGroup(){
        group = verifyCommonValue("group", "FaceGroup", props, log);
    }

	/**
	 * get方法。提供获取配置文件中的值的方法。
	 */

	public static String getAddress() {
		log.info("Load the configuration address, the value is \"" + address + "\"");
		return address;
	}

	public static String getTopic() {
		log.info("Load the configuration topic, the value is \"" + topic + "\"");
		return topic;
	}

	public static String getGroup() {
		log.info("Load the configuration group, the value is \"" + group + "\"");
		return group;
	}

}

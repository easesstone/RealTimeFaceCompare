package com.hzgc.collect.expand.util;

/**
 * ProperHelper的容器类，所有ProperHelper类在这个容器内注册并实例化；
 * 经一次HelperFactory.regist()对所有获取配置文件类实例化后，
 * 就可以通过“类名.get方法”直接获取到对应配置文件中的值。（马燊偲）
 */
public class HelperFactory {
	public static void regist() {
		new ClusterOverFtpProperHelper();
		new FTPAddressProperHelper();
		new ProducerOverFtpProperHelper();
		new RocketMQProperHelper();
	}
}

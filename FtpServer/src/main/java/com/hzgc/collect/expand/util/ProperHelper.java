package com.hzgc.collect.expand.util;

import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * @Title： ProperHelper
 * @Description： 抽象类ProperHelper，提供所有名为“**ProperHelper”类的公用方法
 * @author： 马燊偲
 */

abstract class ProperHelper {

	public static String verifyNoDefaultValue(String key, Properties props, Logger log){
		String returnValue = null;
		Boolean isValueEmpty = props.getProperty(key).isEmpty(); //判断键值是否为空
		if (!isValueEmpty){

			log.warn("There is no need to set " + key + ", set it to null.");
		}
		else {
			log.info("The configuration " + key + " is right, the value is " + returnValue);
		}
		return returnValue;
	}

	/**
	 * 验证配置文件中，无需特殊判断的属性值是否为正确格式。例如用户名user，密码password，路径格式pathRule。
	 * 验证逻辑：若key对应的属性值未设置（为空），则设为默认值；否则获取到该属性值。
	 * @param key 配置文件中，某属性字段的Key
	 * @param defaultValue 配置文件中，某属性字段的默认值
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的属性值
	 */
	public static String verifyCommonValue(String key, String defaultValue, Properties props, Logger log){
		String returnValue = null;
		try {
			Boolean isValueEmpty = props.getProperty(key).isEmpty(); //判断键值是否为空
			if (isValueEmpty){
				returnValue = defaultValue;
				log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
			}
			else {
				String valueFromKey = props.getProperty(key);
				if (valueFromKey == null || valueFromKey == ""){
					returnValue = defaultValue;
					log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue+ "\"");
				}
				//非空，加载
				else {
					log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
					returnValue = valueFromKey;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnValue;
	}

	/**
	 * 验证配置文件中，IP属性字段是否为正确格式。
	 * 验证逻辑为：判断该值是否为空、是否为IP的格式；若key对应的属性值未设置（为空），则设为默认值。
	 * @param ipKey 配置文件中，IP属性字段的Key
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的IP属性字段值
	 */
	public static String verifyIp(String ipKey, Properties props, Logger log){
		return verifyIpOrPlusPort(ipKey, patternIp(), props, log);
	}

	/**
	 * 验证配置文件中，IP：PORT属性字段是否为正确格式。
	 * @param ipKey 配置文件中，IP：PORT属性字段的Key
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的IP：PORT属性字段值
	 */
	public static String verifyIpPlusPort(String ipKey, Properties props, Logger log){
		return verifyIpOrPlusPort(ipKey, patternIpPlusPort(), props, log);
	}

	//验证配置文件中，IP 或 IP：PORT属性字段是否为正确格式的方法。
	private static String verifyIpOrPlusPort(String key, Pattern patternIpOrPlusPortLegal, Properties props, Logger log){
		String returnValue = null;
		try {
			Boolean isValueEmpty = props.getProperty(key).isEmpty();

			if (isValueEmpty){
				log.error("The value of " + key + " haven't been set,  you must set it.");
				System.exit(1);
			}
			else {
				//键值存在，才能getProperty取到值。
				String valueFromKey = props.getProperty(key);
				//判断是否是合法的ip / ip:端口号  格式的正则表达式
				Pattern pattern = patternIpOrPlusPortLegal;

				Boolean isIpOrPlusPortLegal = pattern.matcher(valueFromKey).matches();
				if (!isIpOrPlusPortLegal){
					log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, please reset it.");
					System.exit(1);
				}
				else{
					log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
					returnValue = valueFromKey;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnValue;
	}

	//IP：PORT，IP：PORT，IP：PORT的正则表达式。
	private static Pattern patternIp(){
		String regexIpPlusPort = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		Pattern pattern = Pattern.compile(regexIpPlusPort);
		return pattern;
	}

	//IP：PORT，IP：PORT，IP：PORT的正则表达式。
	private static Pattern patternIpPlusPort(){
		String regexIpPlusPort = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\:"
				+ "([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])$";
		Pattern pattern = Pattern.compile(regexIpPlusPort);
		return pattern;
	}

	/**
	 * 验证配置文件中，port属性字段是否为正确格式。
	 * 验证逻辑：判断该值是否为空、是否大于1024、是否为整数。若为空，设定为默认值。
	 * @param portKey 配置文件中，port属性字段的Key
	 * @param portDefault 配置文件中，port属性字段的默认值
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的Port属性字段值
	 */
	public static String verifyPort(String portKey, String portDefault, Properties props, Logger log) {
		String returnPort = null;
		try {
			Boolean isValueEmpty = props.getProperty(portKey).isEmpty(); //判断键值是否为空

			//若端口号未设置，设置为默认值
			if (isValueEmpty) {
				returnPort = portDefault;
				log.warn("The value of " + portKey + " haven't been set, set it to \"" + portDefault + "\"");
			}
			else {
				//键值存在，才能getProperty取到值。
				String portValue = props.getProperty(portKey);
				//若端口号为空
				if (portValue == null || portValue == "") {
					returnPort = portDefault;
					log.warn("The value of " + portKey + " haven't been set, set it to \"" + portDefault + "\"");
				}
				//若端口号非整数，报错
				else if (!isValueInteger(portValue)) {
					log.error("The value \"" + portValue + "\" of " + portKey + " is illegal, it must be Integer.");
					System.exit(1);
				}
				//若端口号小于1024，报错
				else if (Integer.parseInt(portValue) <= 1024) {
					log.error("The value \"" + portValue + "\"  of " + portKey + " is illegal, it must larger than 1024.");
					System.exit(1);
				}
				//端口号符合条件，加载
				else {
					log.info("The configuration " + portKey + "  is right, the value is \"" + portValue + "\"");
					returnPort = portValue;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnPort;
	}

	/**
	 * 验证配置文件中，布尔值属性字段是否为正确格式。例如implicitSsl。
	 * 验证逻辑：判断该值是否为空、是否为布尔值。若为空，设定为默认值。
	 * @param key 配置文件中，布尔值属性字段的Key
	 * @param defaultValue 配置文件中，布尔值属性字段的默认值
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的布尔属性字段值
	 */
	public static String verifyBooleanValue(String key, String defaultValue, Properties props, Logger log){
		String returnValue = null;
		try {
			Boolean isValueEmpty = props.getProperty(key).isEmpty(); //判断键值是否为空
			if (isValueEmpty){
				returnValue = defaultValue;
				log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
			}
			else {
				String valueFromKey = props.getProperty(key); //读取配置文件中，Key对应的值
				if (valueFromKey == null || valueFromKey == "") {
					returnValue = defaultValue;
					log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
				}
				//检查key=returnValue的value值类型是否为布尔值，比较时忽略大小写；若符合条件，则加载。
				else if (valueFromKey.equalsIgnoreCase("true") || valueFromKey.equalsIgnoreCase("false")) {
					log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
					returnValue = defaultValue;
				}
				else {
					log.error("The value \"" + valueFromKey + "\"  of " + key + " is illegal, it must be Boolean.");
					System.exit(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnValue;
	}

	/**
	 * 验证配置文件中，正整数属性字段是否为正确格式。例如队列线程数thread.number。
	 * 验证逻辑：判断该值是否为空、是否为正整数。若为空，设定为默认值。
	 * @param key 配置文件中，某属性字段的Key
	 * @param defaultValue 配置文件中，某属性字段的默认值
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的Port属性字段值
	 */
	public static String verifyPositiveIntegerValue(String key, String defaultValue, Properties props, Logger log){
		String returnValue = null;
		try {
			Boolean isValueEmpty = props.getProperty(key).isEmpty();
			//若未设置，设置为默认值
			if (isValueEmpty) {
				log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
				returnValue = defaultValue;
			}
			else {
				String valueFromKey = props.getProperty(key);
				if (valueFromKey == null || valueFromKey == "") {
					log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
					returnValue = defaultValue;
				}
				//判断是否是整数
				else if (!isValueInteger(valueFromKey)) {
					log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, it must be Integer.");
					System.exit(1);
				}
				//判断是否大于0
				else if (Integer.parseInt(valueFromKey) <= 0) {
					log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, it must be Integer and larger than 0.");
					System.exit(1);
				}
				//值符合条件，加载
				else {
					log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
					returnValue = valueFromKey;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnValue;
	}

	/**
	 * 验证配置文件中，整数属性字段是否为正确格式。例如队列线程数retries。
	 * 验证逻辑：判断该值是否为空、是否为整数。若为空，设定为默认值。
	 * @param key 配置文件中，某属性字段的Key
	 * @param defaultValue 配置文件中，某属性字段的默认值
	 * @param props 不同的ProperHelper类中传进来的配置文件变量props
	 * @param log 不同的ProperHelper类中传进来的日志变量log
	 * @return 验证格式正确后的属性值
	 */
	public static String verifyIntegerValue(String key, String defaultValue, Properties props, Logger log){
		String returnValue = null;
		try {
			Boolean isValueEmpty = props.getProperty(key).isEmpty();
			//若未设置，设置为默认值
			if (isValueEmpty) {
				log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
				returnValue = defaultValue;
			}
			else {
				String valueFromKey = props.getProperty(key);
				if (valueFromKey == null || valueFromKey == "") {
					log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
					returnValue = defaultValue;
				}
				//判断是否是整数
				else if (!isValueInteger(valueFromKey)) {
					log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, it must be Integer.");
					System.exit(1);
				}
				//值符合条件，加载
				else {
					log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
					returnValue = valueFromKey;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.error("catch an unknown error.");
		}
		return returnValue;
	}

	//判断字符串表示的值是否是整数。
	private static Boolean isValueInteger(String value) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		Boolean valueIsInteger = pattern.matcher(value).matches();
		//判断是否是整数
		if (valueIsInteger)
			return true;
		else
			return false;
	}

}

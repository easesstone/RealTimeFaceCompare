package com.hzgc.collect.expand.util;

import org.apache.log4j.Logger;


import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 此抽象类定义了资源文件对应的工具类中的公有方法
 * 每一个子类都对应一个配置文件
 * 用来从配置文件中读取配置（马燊偲）
 */

abstract class ProperHelper {

    /**
     * 验证配置文件中，无需特殊判断的属性值是否为正确格式。例如用户名user，密码password，路径格式pathRule。
     * 验证逻辑：若key对应的属性值未设置（为空），则设为默认值；否则获取到该属性值。
     * @param key 配置文件中，某属性字段的Key
     * @param defaultValue 配置文件中，某属性字段的默认值
     * @param props 不同的ProperHelper类中传进来的配置文件变量props
     * @param log 不同的ProperHelper类中传进来的日志变量log
     * @return 验证格式正确后的属性值
     */
    static String verifyCommonValue(String key, String defaultValue, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty(); //判断键值是否为空
                if (isValueEmpty) {
                    returnValue = defaultValue;
                    log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                } else {
                    String valueFromKey = props.getProperty(key);
                    if (valueFromKey == null || Objects.equals(valueFromKey, "")) {
                        returnValue = defaultValue;
                        log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                    }
                    //非空，加载
                    else {
                        log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
                        returnValue = valueFromKey;
                    }
                }
            }
            else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
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
    static String verifyIp(String ipKey, Properties props, Logger log){
        return verifyIpOrPlusPort(ipKey, patternIp(), props, log);
    }

    /**
     * 验证配置文件中，IP：PORT属性字段是否为正确格式。
     * @param ipKey 配置文件中，IP：PORT属性字段的Key
     * @param props 不同的ProperHelper类中传进来的配置文件变量props
     * @param log 不同的ProperHelper类中传进来的日志变量log
     * @return 验证格式正确后的IP：PORT属性字段值
     */
    static String verifyIpPlusPort(String ipKey, Properties props, Logger log){
        return verifyIpOrPlusPort(ipKey, patternIpPlusPort(), props, log);
    }

    /**
     * 验证配置文件中，IP 或 IP：PORT属性字段是否为正确格式的方法。
     *
     * @param key                      配置文件中，某属性字段的Key
     * @param patternIpOrPlusPortLegal IP或IP：PORT对应的正则表达式
     * @param props                    不同的ProperHelper类中传进来的配置文件变量props
     * @param log                      不同的ProperHelper类中传进来的日志变量log
     * @return 验证格式正确后的属性字段值
     */
    private static String verifyIpOrPlusPort(String key, Pattern patternIpOrPlusPortLegal, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty();

                if (isValueEmpty) {
                    log.error("The value of " + key + " haven't been set,  you must set it.");
                    System.exit(1);
                } else {
                    //键值存在，才能getProperty取到值。
                    String valueFromKey = props.getProperty(key);
                    //判断是否是合法的ip / ip:端口号  格式的正则表达式
                    Boolean isIpOrPlusPortLegal = patternIpOrPlusPortLegal.matcher(valueFromKey).matches();
                    if (!isIpOrPlusPortLegal) {
                        log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, please reset it.");
                        System.exit(1);
                    } else {
                        log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
                        returnValue = valueFromKey;
                    }
                }
            }
            else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("catch an unknown error.");
        }
        return returnValue;
    }

    /**
     * IP的正则表达式
     *
     * @return 正则表达式
     */
    private static Pattern patternIp(){
        String regexIpPlusPort = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        return Pattern.compile(regexIpPlusPort);
    }

    /**
     * IP：PORT的正则表达式。
     *
     * @return 正则表达式
     */
    private static Pattern patternIpPlusPort(){
        String regexIpPlusPort = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\:"
                + "([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])$";
        return Pattern.compile(regexIpPlusPort);
    }

    /**
     * 验证配置文件中，“IP：PORT，IP：PORT，IP：PORT”属性字段是否为正确格式的方法。
     * @param key 配置文件中，某属性字段的Key
     * @param props 不同的ProperHelper类中传进来的配置文件变量props
     * @param log 不同的ProperHelper类中传进来的日志变量log
     * @return 验证格式正确后的属性字段值
     */
    static String verifyIpPlusPortList(String key, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty();
                if (isValueEmpty) {
                    log.error("The value of " + key + " haven't been set,  you must set it.");
                    System.exit(1);
                } else {
                    //键值存在，才能getProperty取到值（去一下空格）。
                    String valueFromKey = props.getProperty(key).trim();
                    //对于“IP：PORT，IP：PORT，IP：PORT”中的每个IP：PORT（以逗号分隔）：
                    int count = 0;
                    for (String ipPlusPort : valueFromKey.split(",")) {
                        //判断每个IP：PORT 是否是合法的 ip:端口号  格式的正则表达式
                        Boolean isIpPlusPortLegal = patternIpPlusPort().matcher(ipPlusPort).matches();
                        //若有一个IP：PORT 的格式不正确，就报错，退出程序
                        if (!isIpPlusPortLegal) {
                            log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, please reset it.");
                            System.exit(1);
                        }
                        //记录IP：PORT 的格式正确的个数
                        else {
                            count++;
                        }
                    }
                    //若每个以逗号分隔的IP：PORT 的格式都正确，且“IP：PORT，IP：PORT，IP：PORT”不以逗号结尾，则格式正确。
                    if (count == valueFromKey.split(",").length && !valueFromKey.endsWith(",")) {
                        log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
                        returnValue = valueFromKey;
                    } else {
                        log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, please reset it.");
                        System.exit(1);
                    }
                }
            }
            else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("catch an unknown error.");
        }
        return returnValue;
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
    static String verifyPort(String portKey, String portDefault, Properties props, Logger log) {
        String returnPort = null;
        try {
            if (props.containsKey(portKey)) {
                Boolean isValueEmpty = props.getProperty(portKey).isEmpty(); //判断键值是否为空

                //若端口号未设置，设置为默认值
                if (isValueEmpty) {
                    returnPort = portDefault;
                    log.warn("The value of " + portKey + " haven't been set, set it to \"" + portDefault + "\"");
                } else {
                    //键值存在，才能getProperty取到值。
                    String portValue = props.getProperty(portKey);
                    //若端口号为空
                    if (portValue == null || Objects.equals(portValue, "")) {
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
            }
            else {
                log.warn("The key " + portKey + " does not exist in the configuration file, please check it.");
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
    static String verifyBooleanValue(String key, String defaultValue, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty(); //判断键值是否为空
                if (isValueEmpty) {
                    returnValue = defaultValue;
                    log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                } else {
                    String valueFromKey = props.getProperty(key); //读取配置文件中，Key对应的值
                    if (valueFromKey == null || Objects.equals(valueFromKey, "")) {
                        returnValue = defaultValue;
                        log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                    }
                    //检查key=returnValue的value值类型是否为布尔值，比较时忽略大小写；若符合条件，则加载。
                    else if (valueFromKey.equalsIgnoreCase("true") || valueFromKey.equalsIgnoreCase("false")) {
                        log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
                        returnValue = valueFromKey;
                    } else {
                        log.error("The value \"" + valueFromKey + "\"  of " + key + " is illegal, it must be Boolean.");
                        System.exit(1);
                    }
                }
            }
            else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
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
    static String verifyPositiveIntegerValue(String key, String defaultValue, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty();
                //若未设置，设置为默认值
                if (isValueEmpty) {
                    log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                    returnValue = defaultValue;
                } else {
                    String valueFromKey = props.getProperty(key);
                    if (valueFromKey == null || Objects.equals(valueFromKey, "")) {
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
            }
            else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
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
    static String verifyIntegerValue(String key, String defaultValue, Properties props, Logger log){
        String returnValue = null;
        try {
            if (props.containsKey(key)) {
                Boolean isValueEmpty = props.getProperty(key).isEmpty();
                //若未设置，设置为默认值
                if (isValueEmpty) {
                    log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                    returnValue = defaultValue;
                } else {
                    String valueFromKey = props.getProperty(key);
                    if (valueFromKey == null || Objects.equals(valueFromKey, "")) {
                        log.warn("The value of " + key + " haven't been set, set it to \"" + defaultValue + "\"");
                        returnValue = defaultValue;
                    }
                    //判断是否是整数
                    else if (!isValueInteger(valueFromKey)) {
                        log.error("The value \"" + valueFromKey + "\" of " + key + " is illegal, it must be Integer.");
                        System.exit(1);
                    } else { //值符合条件，加载
                        log.info("The configuration " + key + " is right, the value is \"" + valueFromKey + "\"");
                        returnValue = valueFromKey;
                    }
                }
            } else {
                log.warn("The key " + key + " does not exist in the configuration file, please check it.");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("catch an unknown error.");
        }
        return returnValue;
    }

    /**
     * 判断字符串表示的值是否是整数。
     *
     * @param value 需要判断的字符串值
     * @return 表示字符串值是否为整数的布尔值
     */
    private static Boolean isValueInteger(String value) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        //判断是否是整数
        return pattern.matcher(value).matches();
    }

}

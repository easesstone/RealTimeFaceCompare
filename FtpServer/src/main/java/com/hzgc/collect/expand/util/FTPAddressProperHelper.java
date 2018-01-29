package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 从配置文件ftpAddress.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class FTPAddressProperHelper extends ProperHelper{

    private static Logger log = Logger.getLogger(FTPAddressProperHelper.class);
    private static Properties props = new Properties();

    private static String ip;
    private static String port;
    private static String user;
    private static String password;
    private static String pathRule;

    static {
        String properName = "ftpAddress.properties";
        try {
            props.load(new FileInputStream(FileUtil.loadResourceFile(properName)));
            log.info("Load configuration for ftp Server from ./conf/ftpAddress.properties");

            setIp();
            setPort();
            setUser();
            setPassword();
            setPathRule();

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Catch an unknown error, can't load the configuration file" + properName);
        }
    }

    /**
     * set方法。验证配置文件中的值是否为符合条件的格式。
     */
    private static void setIp() {
        ip = verifyIp("ip", props, log);
    }

    private static void setPort() {
        port = verifyPort("port", "2121", props, log);
    }

    private static void setUser() {
        user = verifyCommonValue("user","admin", props, log);
    }

    private static void setPassword() {
        password = verifyCommonValue("password", "123456", props, log);
    }

    private static void setPathRule() {
        pathRule = verifyCommonValue("pathRule","%f/%Y/%m/%d/%H", props, log);
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public static String getIp() {
        log.info("Load the configuration ip, the value is \"" + ip + "\"");
        System.out.println(ip);
        return ip;
    }

    public static String getPort() {
        log.info("Load the configuration port, the value is \"" + port + "\"");
        return port;
    }

    public static String getUser() {
        log.info("Load the configuration user, the value is \"" + user + "\"");
        return user;
    }

    public static String getPassword() {
        log.info("Load the configuration password, the value is \"" + password + "\"");
        return password;
    }

    public static String getPathRule() {
        log.info("Load the configuration pathRule, the value is \"" + pathRule + "\"");
        return pathRule;
    }
}

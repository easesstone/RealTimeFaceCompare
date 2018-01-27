package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 从配置文件cluster-over-ftp.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class ClusterOverFtpProperHelper extends ProperHelper {
    private static Logger log = Logger.getLogger(ClusterOverFtpProperHelper.class);
    private static Properties props = new Properties();
    private static String port;
    private static String implicitSsl;
    private static String threadNum;

    static {
        String properName = "cluster-over-ftp.properties";
        try {
            props.load(new FileInputStream(FileUtil.loadResourceFile(properName)));
            log.info("Load configuration for ftp server from ./conf/cluster-over-ftp.properties");

            setPort();
            setImplicitSsl();
            setThreadNum();

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Catch an unknown error, can't load the configuration file" + properName);
        }
    }

    /**
     * set方法。验证配置文件中的值是否为符合条件的格式。
     */

    private static void setPort() {
        port = verifyPort("listener-port", "2121", props, log);
    }

    private static void setImplicitSsl() {
        implicitSsl = verifyBooleanValue("implicitSsl", "false", props, log);
    }

    private static void setThreadNum() {
        threadNum = verifyPositiveIntegerValue("thread.number", "3", props, log);
    }


    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public static String getPort() {
        log.info("Load the configuration listener-port, the value is \"" + port + "\"");
        return port;
    }

    public static String getImplicitSsl() {
        log.info("Load the configuration implicitSsl, the value is \"" + implicitSsl + "\"");
        return implicitSsl;
    }

    public static String getThreadNum() {
        log.info("Load the configuration thread.number, the value is \"" + threadNum + "\"");
        return threadNum;
    }
}


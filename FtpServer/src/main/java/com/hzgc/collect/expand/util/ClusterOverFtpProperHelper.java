package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
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
    private static String dataPorts;
    private static String implicitSsl;
    private static String threadNum;
    private static String capacity;
    private static String receiveLogDir;
    private static String processLogDir;
    private static String receiveNumber;

    static {
        String properName = "cluster-over-ftp.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
                log.info("Load configuration for ftp server from ./conf/cluster-over-ftp.properties");

                setPort();
                setDataPorts();
                setImplicitSsl();
                setThreadNum();
                setCapacity();
                setReceiveLogDir();
                setProcessLogDir();
                setReceiveNumber();
            } else {
                log.error("The property file " + properName + "doesn't exist!");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Catch an unknown error, can't load the configuration file" + properName);
        } finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * set方法。验证配置文件中的值是否为符合条件的格式。
     */

    private static void setPort() {
        port = verifyPort("listener-port", "2121", props, log);
    }

    private static void setDataPorts() {
        dataPorts = verifyCommonValue("data-ports", "2223-2225", props, log);
    }

    private static void setImplicitSsl() {
        implicitSsl = verifyBooleanValue("implicitSsl", "false", props, log);
    }

    private static void setThreadNum() {
        threadNum = verifyPositiveIntegerValue("thread.number", "3", props, log);
    }

    private static void setCapacity() {
        capacity = verifyPositiveIntegerValue("capacity", String.valueOf(Integer.MAX_VALUE), props, log);
    }

    private static void setReceiveLogDir() {
        receiveLogDir = verifyCommonValue("receiveLogDir", "/opt/", props, log);
    }

    private static void setProcessLogDir() {
        processLogDir = verifyCommonValue("processLogDir", "/opt/", props, log);
    }

    private static void setReceiveNumber() {
        receiveNumber = verifyCommonValue("receiveNumber", String.valueOf(0), props, log);
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public static Integer getPort() {
        log.info("Load the configuration listener-port, the value is \"" + port + "\"");
        return Integer.valueOf(port);
    }

    public static String getDataPorts() {
        log.info("Load the configuration data-ports, the value is \"" + dataPorts + "\"");
        return dataPorts;
    }

    public static Boolean getImplicitSsl() {
        log.info("Load the configuration implicitSsl, the value is \"" + implicitSsl + "\"");
        return Boolean.valueOf(implicitSsl);
    }

    public static Integer getThreadNum() {
        log.info("Load the configuration thread.number, the value is \"" + threadNum + "\"");
        return Integer.valueOf(threadNum);
    }

    public static Integer getCapacity() {
        log.info("Load the configuration capacity, the value is \"" + capacity + "\"");
        return Integer.valueOf(capacity);
    }

    public static String getReceiveLogDir() {
        log.info("Load the configuration receiveLogDir, the value is \"" + receiveLogDir + "\"");
        return receiveLogDir;
    }

    public static String getProcessLogDir() {
        log.info("Load the configuration processLogDir, the value is \"" + processLogDir + "\"");
        return processLogDir;
    }

    public static Integer getReceiveNumber() {
        log.info("Load the configuration receiveNumber, the value is \"" + receiveNumber + "\"");
        return Integer.valueOf(receiveNumber);
    }

    /**
     * 获取Properties属性的资源文件变量
     */
    public static Properties getProps(){
        log.info("Load configuration file ./conf/cluster-over-ftp.properties：" + props);
        return props;
    }
}


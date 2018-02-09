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

    private static String logSize;
    private static String receiveQueueCapacity;
    private static String receiveLogDir;
    private static String processLogDir;
    private static String successLogDir;
    private static String mergeLogDir;
    private static String receiveNumber;
    private static String mergeScanTime;
    private static String faceDetectorNumber;
    private static String port;
    private static String dataPorts;
    private static String implicitSsl;
    private static String ftpdataDir;

    static {
        String properName = "cluster-over-ftp.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
                log.info("Load configuration for ftp server from ./conf/cluster-over-ftp.properties");

                setLogSize();
                setReceiveQueueCapacity();
                setReceiveLogDir();
                setProcessLogDir();
                setSuccessLogDir();
                setMergeLogDir();
                setReceiveNumber();
                setMergeScanTime();
                setFaceDetectorNumber();
                setPort();
                setDataPorts();
                setImplicitSsl();
                setFtpdataDir();
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

    private static void setReceiveQueueCapacity() {
        receiveQueueCapacity = verifyPositiveIntegerValue("receive.queue.capacity", String.valueOf(Integer.MAX_VALUE), props, log);
    }

    private static void setReceiveLogDir() {
        receiveLogDir = verifyCommonValue("receive.log.dir", "/opt/RealTimeFaceCompare/ftp/data/receive", props, log);
    }

    private static void setProcessLogDir() {
        processLogDir = verifyCommonValue("process.log.dir", "/opt/RealTimeFaceCompare/ftp/data/process", props, log);
    }

    private static void setSuccessLogDir() {
        successLogDir = verifyCommonValue("success.log.dir","/opt/RealTimeFaceCompare/ftp/success", props, log);
    }

    private static void setMergeLogDir() {
        mergeLogDir = verifyCommonValue("merge.log.dir", "/opt/RealTimeFaceCompare/ftp/merge", props, log);
    }

    private static void setMergeScanTime() {
        mergeScanTime = verifyPositiveIntegerValue("merge.scan.time", "", props, log);
    }

    private static void setReceiveNumber() {
        receiveNumber = verifyCommonValue("receive.number", "6", props, log);
    }

    private static void setLogSize() {
        logSize = verifyPositiveIntegerValue("log.Size", "300000", props, log);
    }

    private static void setFaceDetectorNumber() {
        faceDetectorNumber = verifyPositiveIntegerValue("face.detector.number","", props, log);
    }

    public static void setFtpdataDir() {
        ftpdataDir = verifyCommonValue("ftp.data.dir","/opt/ftpdata",props,log);
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

    public static Integer getReceiveQueueCapacity() {
        log.info("Load the configuration receive.queue.capacity, the value is \"" + receiveQueueCapacity + "\"");
        return Integer.valueOf(receiveQueueCapacity);
    }

    public static String getReceiveLogDir() {
        log.info("Load the configuration receive.log.dir, the value is \"" + receiveLogDir + "\"");
        return receiveLogDir;
    }

    public static String getProcessLogDir() {
        log.info("Load the configuration process.log.dir, the value is \"" + processLogDir + "\"");
        return processLogDir;
    }

    public static Integer getReceiveNumber() {
        log.info("Load the configuration receive.number, the value is \"" + receiveNumber + "\"");
        return Integer.valueOf(receiveNumber);
    }

    public static String getSuccessLogDir() {
        log.info("Load the configuration success.log.dir, the value is \"" + successLogDir + "\"");
        return successLogDir;
    }

    public static String getMergeLogDir() {
        log.info("Load the configuration merge.log.dir, the value is \"" + mergeLogDir + "\"");
        return mergeLogDir;
    }

    public static Integer getMergeScanTime() {
        log.info("Load the configuration merge.scan.time, the value is \"" + mergeScanTime + "\"");
        return Integer.valueOf(mergeScanTime);
    }

    public static Integer getLogSize() {
        log.info("Load the configuration log.Size, the value is \"" + logSize + "\"");
        return Integer.valueOf(logSize);
    }

    public static Integer getFaceDetectorNumber() {
        log.info("Load the configuration face.detector.number, the value is \"" + faceDetectorNumber + "\"");
        return Integer.valueOf(faceDetectorNumber);
    }

    public static String getFtpdataDir() {
        log.info("Load the configuration ftp.data.dir, the value is \"" + ftpdataDir + "\"");
        return ftpdataDir;
    }

    /**
     * 获取Properties属性的资源文件变量
     */
    public static Properties getProps(){
        log.info("Load configuration file ./conf/cluster-over-ftp.properties：" + props);
        return props;
    }
}

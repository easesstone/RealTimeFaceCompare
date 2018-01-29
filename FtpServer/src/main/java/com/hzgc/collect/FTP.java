package com.hzgc.collect;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.HelperFactory;
import com.hzgc.collect.ftp.ClusterOverFtp;
import com.hzgc.collect.ftp.ConnectionConfigFactory;
import com.hzgc.collect.ftp.FtpServer;
import com.hzgc.collect.ftp.FtpServerFactory;
import com.hzgc.collect.ftp.util.LoggerConfig;
import com.hzgc.util.common.FileUtil;
import com.hzgc.collect.ftp.command.CommandFactoryFactory;
import com.hzgc.collect.ftp.nativefs.filesystem.NativeFileSystemFactory;
import com.hzgc.collect.ftp.ftplet.FtpException;
import com.hzgc.collect.ftp.listener.ListenerFactory;
import com.hzgc.collect.ftp.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class FTP extends ClusterOverFtp {
    private static Logger log = Logger.getLogger(FTP.class);

    private static Map<Integer, Integer> pidMap = new HashMap<>();

    /*
      Set the dynamic log configuration file refresh time
     */
    static {
        //加载所有配置文件
        HelperFactory.regist();
        new LoggerConfig();
    }

    @Override
    public void startFtpServer() {
        //expand模块的公共Conf对象
        CommonConf commonConf = new CommonConf();
        //使用带CommonConf对象的有参构造器可以构造带有expand模块的FtpServerContext
        FtpServerFactory serverFactory = new FtpServerFactory(commonConf);
        log.info("Create " + FtpServerFactory.class + " successful");
        ListenerFactory listenerFactory = new ListenerFactory();
        log.info("Create " + ListenerFactory.class + " successful");
        //set the port of the listener
        listenerFactory.setPort(listenerPort);
        log.info("The port for listener is " + listenerPort);
        // replace the default listener
        serverFactory.addListener("default", listenerFactory.createListener());
        log.info("Add listner, name:default, class:" + serverFactory.getListener("default").getClass());
        // set customer user manager
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        try {
            userManagerFactory.setFile(FileUtil.loadResourceFile("users.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        log.info("Set customer user manager factory is successful, " + userManagerFactory.getClass());
        //set customer cmd factory
        CommandFactoryFactory commandFactoryFactory = new CommandFactoryFactory();
        serverFactory.setCommandFactory(commandFactoryFactory.createCommandFactory());
        log.info("Set customer command factory is successful, " + commandFactoryFactory.getClass());
        //set local file system
        NativeFileSystemFactory nativeFileSystemFactory = new NativeFileSystemFactory();
        serverFactory.setFileSystem(nativeFileSystemFactory);
        log.info("Set customer file system factory is successful, " + nativeFileSystemFactory.getClass());
        // TODO: 2017-10-9
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        log.info("FTP Server Maximum logon number:" + connectionConfigFactory.createUDConnectionConfig().getMaxLogins());
        serverFactory.setConnectionConfig(connectionConfigFactory.createUDConnectionConfig());
        log.info("Set user defined connection config file is successful, " + connectionConfigFactory.getClass());
        FtpServer server = serverFactory.createServer();
        try {
            server.start();
            Integer ftpPID = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            pidMap.put(ftpPID, listenerPort);
        } catch (FtpException e) {
            e.printStackTrace();
        }

    }

    public static Map<Integer, Integer> getPidMap() {
        return pidMap;
    }

    public static void main(String args[]) throws Exception {
        FTP ftp = new FTP();
        ftp.loadConfig();
        ftp.startFtpServer();
    }
}

package com.hzgc.ftpserver.local;

import com.hzgc.ftpserver.ClusterOverFtp;
import com.hzgc.ftpserver.util.LoggerConfig;
import com.hzgc.util.FileUtil;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.log4j.Logger;

public class LocalOverFtpServer extends ClusterOverFtp {
    private static Logger log = Logger.getLogger(LocalOverFtpServer.class);

    /*
      Set the dynamic log configuration file refresh time
     */
    static {
        new LoggerConfig();
    }

    @Override
    public void startFtpServer() {
        LocalFtpServerFactory serverFactory = new LocalFtpServerFactory();
        log.info("Create " + LocalFtpServerFactory.class + " successful");
        ListenerFactory listenerFactory = new ListenerFactory();
        log.info("Create " + ListenerFactory.class + " successful");
        //set the port of the listener
        listenerFactory.setPort(listenerPort);
        log.info("The port for listener is " + listenerPort);
        // replace the default listener
        serverFactory.addListener("default", listenerFactory.createListener());
        log.info("Add listner, name:default, class:" + serverFactory.getListener("default").getClass());
        // set customer user manager
        LocalPropertiesUserManagerFactory userManagerFactory = new LocalPropertiesUserManagerFactory();
        try {
            userManagerFactory.setFile(FileUtil.loadResourceFile("users.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        log.info("Set customer user manager factory is successful, " + userManagerFactory.getClass());
        //set customer cmd factory
        LocalCmdFactoryFactory cmdFactoryFactory = new LocalCmdFactoryFactory();
        serverFactory.setCommandFactory(cmdFactoryFactory.createCommandFactory());
        log.info("Set customer command factory is successful, " + cmdFactoryFactory.getClass());
        //set local file system
        LocalFileSystemFactory localFileSystemFactory = new LocalFileSystemFactory();
        serverFactory.setFileSystem(localFileSystemFactory);
        log.info("Set customer file system factory is successful, " + localFileSystemFactory.getClass());
        // TODO: 2017-10-9
        LocalConnectionConfigFactory connectionConfigFactory = new LocalConnectionConfigFactory();
        log.info("FTP Server Maximum logon number:" + connectionConfigFactory.createUDConnectionConfig().getMaxLogins());
        serverFactory.setConnectionConfig(connectionConfigFactory.createUDConnectionConfig());
        log.info("Set user defined connection config file is successful, " + connectionConfigFactory.getClass());
        FtpServer server = serverFactory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) throws Exception {
        LocalOverFtpServer localOverFtpServer = new LocalOverFtpServer();
        localOverFtpServer.loadConfig();
        localOverFtpServer.startFtpServer();
    }
}

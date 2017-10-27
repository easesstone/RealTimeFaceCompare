package com.hzgc.ftpserver.kafka.ftp;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.ftpletcontainer.impl.DefaultFtpletContainer;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.message.MessageResource;

import java.io.Serializable;
import java.util.Map;

public class KafkaFtpServerFactory extends FtpServerFactory implements Serializable {
    private KafkaFtpServerContext kafkaFtpServerContext;

    public KafkaFtpServerFactory() {
        kafkaFtpServerContext = new KafkaFtpServerContext();
    }


    @Override
    public FtpServer createServer() {
        return new DefaultFtpServer(kafkaFtpServerContext);
    }


    @Override
    public Map<String, Listener> getListeners() {
        return kafkaFtpServerContext.getListeners();
    }


    @Override
    public Listener getListener(final String name) {
        return kafkaFtpServerContext.getListener(name);
    }


    @Override
    public void addListener(final String name, final Listener listener) {
        kafkaFtpServerContext.addListener(name, listener);
    }


    @Override
    public void setListeners(final Map<String, Listener> listeners) {
        kafkaFtpServerContext.setListeners(listeners);
    }


    @Override
    public Map<String, Ftplet> getFtplets() {
        return kafkaFtpServerContext.getFtpletContainer().getFtplets();
    }

    @Override
    public void setFtplets(final Map<String, Ftplet> ftplets) {
        kafkaFtpServerContext.setFtpletContainer(new DefaultFtpletContainer(ftplets));
    }

    @Override
    public UserManager getUserManager() {
        return kafkaFtpServerContext.getUserManager();
    }

    @Override
    public void setUserManager(final UserManager userManager) {
        kafkaFtpServerContext.setUserManager(userManager);
    }

    @Override
    public FileSystemFactory getFileSystem() {
        return kafkaFtpServerContext.getFileSystemManager();
    }

    @Override
    public void setFileSystem(final FileSystemFactory fileSystem) {
        kafkaFtpServerContext.setFileSystemManager(fileSystem);
    }

    @Override
    public CommandFactory getCommandFactory() {
        return kafkaFtpServerContext.getCommandFactory();
    }


    @Override
    public void setCommandFactory(final CommandFactory commandFactory) {
        kafkaFtpServerContext.setCommandFactory(commandFactory);
    }

    @Override
    public MessageResource getMessageResource() {
        return kafkaFtpServerContext.getMessageResource();
    }

    @Override
    public void setMessageResource(final MessageResource messageResource) {
        kafkaFtpServerContext.setMessageResource(messageResource);
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return kafkaFtpServerContext.getConnectionConfig();
    }

    @Override
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        kafkaFtpServerContext.setConnectionConfig(connectionConfig);
    }

}

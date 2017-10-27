package com.hzgc.ftpserver.local;


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

public class LocalFtpServerFactory extends FtpServerFactory implements Serializable {
    private LocalFtpServerContext localFtpServerContext;

    public LocalFtpServerFactory() {
        localFtpServerContext = new LocalFtpServerContext();
    }

    public FtpServer createServer() {
        return new DefaultFtpServer(localFtpServerContext);
    }


    public Map<String, Listener> getListeners() {
        return localFtpServerContext.getListeners();
    }


    public Listener getListener(final String name) {
        return localFtpServerContext.getListener(name);
    }


    public void addListener(final String name, final Listener listener) {
        localFtpServerContext.addListener(name, listener);
    }


    public void setListeners(final Map<String, Listener> listeners) {
        localFtpServerContext.setListeners(listeners);
    }


    public Map<String, Ftplet> getFtplets() {
        return localFtpServerContext.getFtpletContainer().getFtplets();
    }

    public void setFtplets(final Map<String, Ftplet> ftplets) {
        localFtpServerContext.setFtpletContainer(new DefaultFtpletContainer(ftplets));
    }

    public UserManager getUserManager() {
        return localFtpServerContext.getUserManager();
    }

    public void setUserManager(final UserManager userManager) {
        localFtpServerContext.setUserManager(userManager);
    }

    public FileSystemFactory getFileSystem() {
        return localFtpServerContext.getFileSystemManager();
    }

    public void setFileSystem(final FileSystemFactory fileSystem) {
        localFtpServerContext.setFileSystemManager(fileSystem);
    }

    public CommandFactory getCommandFactory() {
        return localFtpServerContext.getCommandFactory();
    }


    public void setCommandFactory(final CommandFactory commandFactory) {
        localFtpServerContext.setCommandFactory(commandFactory);
    }

    public MessageResource getMessageResource() {
        return localFtpServerContext.getMessageResource();
    }

    public void setMessageResource(final MessageResource messageResource) {
        localFtpServerContext.setMessageResource(messageResource);
    }

    public ConnectionConfig getConnectionConfig() {
        return localFtpServerContext.getConnectionConfig();
    }

    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        localFtpServerContext.setConnectionConfig(connectionConfig);
    }
}

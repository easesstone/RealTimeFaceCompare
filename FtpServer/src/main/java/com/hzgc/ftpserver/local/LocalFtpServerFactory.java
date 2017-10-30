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

    @Override
    public FtpServer createServer() {
        return new DefaultFtpServer(localFtpServerContext);
    }


    @Override
    public Map<String, Listener> getListeners() {
        return localFtpServerContext.getListeners();
    }


    @Override
    public Listener getListener(final String name) {
        return localFtpServerContext.getListener(name);
    }


    @Override
    public void addListener(final String name, final Listener listener) {
        localFtpServerContext.addListener(name, listener);
    }


    @Override
    public void setListeners(final Map<String, Listener> listeners) {
        localFtpServerContext.setListeners(listeners);
    }


    @Override
    public Map<String, Ftplet> getFtplets() {
        return localFtpServerContext.getFtpletContainer().getFtplets();
    }

    @Override
    public void setFtplets(final Map<String, Ftplet> ftplets) {
        localFtpServerContext.setFtpletContainer(new DefaultFtpletContainer(ftplets));
    }

    @Override
    public UserManager getUserManager() {
        return localFtpServerContext.getUserManager();
    }

    @Override
    public void setUserManager(final UserManager userManager) {
        localFtpServerContext.setUserManager(userManager);
    }

    @Override
    public FileSystemFactory getFileSystem() {
        return localFtpServerContext.getFileSystemManager();
    }

    @Override
    public void setFileSystem(final FileSystemFactory fileSystem) {
        localFtpServerContext.setFileSystemManager(fileSystem);
    }

    @Override
    public CommandFactory getCommandFactory() {
        return localFtpServerContext.getCommandFactory();
    }


    @Override
    public void setCommandFactory(final CommandFactory commandFactory) {
        localFtpServerContext.setCommandFactory(commandFactory);
    }

    @Override
    public MessageResource getMessageResource() {
        return localFtpServerContext.getMessageResource();
    }

    @Override
    public void setMessageResource(final MessageResource messageResource) {
        localFtpServerContext.setMessageResource(messageResource);
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return localFtpServerContext.getConnectionConfig();
    }

    @Override
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        localFtpServerContext.setConnectionConfig(connectionConfig);
    }
}

package com.hzgc.ftpserver.kafka.ftp;

import org.apache.ftpserver.ConnectionConfig;

import java.io.Serializable;


public class KafkaConnectionConfig implements ConnectionConfig, Serializable {
    private int maxLogins = 10;

    private boolean anonymousLoginEnabled = true;

    private int maxAnonymousLogins = 10;

    private int maxLoginFailures = 3;

    private int loginFailureDelay = 500;

    private int maxThreads = 0;


    public KafkaConnectionConfig(boolean anonymousLoginEnabled,
                                 int loginFailureDelay, int maxLogins, int maxAnonymousLogins,
                                 int maxLoginFailures, int maxThreads) {
        this.anonymousLoginEnabled = anonymousLoginEnabled;
        this.loginFailureDelay = loginFailureDelay;
        this.maxLogins = maxLogins;
        this.maxAnonymousLogins = maxAnonymousLogins;
        this.maxLoginFailures = maxLoginFailures;
        this.maxThreads = maxThreads;
    }

    @Override
    public int getLoginFailureDelay() {
        return loginFailureDelay;
    }

    @Override
    public int getMaxAnonymousLogins() {
        return maxAnonymousLogins;
    }

    @Override
    public int getMaxLoginFailures() {
        return maxLoginFailures;
    }

    @Override
    public int getMaxLogins() {
        return maxLogins;
    }

    @Override
    public boolean isAnonymousLoginEnabled() {
        return anonymousLoginEnabled;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }
}

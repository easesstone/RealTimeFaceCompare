package com.hzgc.collect.ftp.impl;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.receiver.ReceiverScheduler;

public class ReceiverFtpServerContext extends DefaultFtpServerContext {
    private ReceiverScheduler scheduler;

    public ReceiverFtpServerContext(CommonConf conf) {
        super();
        this.scheduler = new ReceiverScheduler(conf);
    }

    @Override
    public ReceiverScheduler getScheduler() {
        return this.scheduler;
    }
}

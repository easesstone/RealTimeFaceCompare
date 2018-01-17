package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.RecvicerConf;
import com.hzgc.collect.expand.reciver.RecvicerEvent;

public class DataProcessLogGroupWriter extends AbstractLogGroupWrite {
    public DataProcessLogGroupWriter(RecvicerConf conf) {
        super(conf);
    }

    @Override
    public void writeEvent(RecvicerEvent event) {
    }

}

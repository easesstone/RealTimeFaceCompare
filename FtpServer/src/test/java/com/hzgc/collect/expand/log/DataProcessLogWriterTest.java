package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Test;


public class DataProcessLogWriterTest {
    @Test
    public void errorLogWrite() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        String queueID = "25";
        conf.setProcessLogDir("E:/getProcessLogDir/process/");
        conf.setReceiveLogDir("E:/getProcessLogDir/receive/");
        String errorLogDir = "E:/getProcessLogDir/process/process-" + queueID + "/error";
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        LogEvent logEvent = new LogEvent();
        logEvent.setStatus("0");
        logEvent.setTimeStamp(1500012313);
        logEvent.setPath("ftp://s100:/2018/01/09");
        for (int i = 0; i < 100000; i++) {
            dataProcessLogWriter.errorLogWrite(errorLogDir, logEvent);
        }
    }
}
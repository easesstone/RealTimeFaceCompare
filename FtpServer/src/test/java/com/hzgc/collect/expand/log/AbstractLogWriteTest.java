package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Test;

/**
 * AbstractLogWriteTest 类测试方法
 *
 * 彭聪
 */
public class AbstractLogWriteTest {
    @Test
    public void prepare() throws Exception {

    }

    @Test
    public void writevent() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        String queueID = "25";
        conf.setProcessLogDir("E:/getProcessLogDir/process");
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        //writerOneLine(dataProcessLogWriter);
        //生成两个日志文件，一个30w行，一个1行，测试日志文件自动生成
        //writeMultiLine(dataProcessLogWriter, 300001);
        //测试往已经存在的空日志文件中写日志
        for (int i = 0; i < 10; i++) {
            writeMultiLine(dataProcessLogWriter, 10);
        }
    }

    private void writerOneLine(DataProcessLogWriter dataProcessLogWriter) {
        LogEvent logEvent = new LogEvent();
        logEvent.setStatus("0");
        logEvent.setTimeStamp(1500012313);
        logEvent.setPath("ftp://s100:/2018/01/09");
        dataProcessLogWriter.writeEvent(logEvent);
    }

    private void writeMultiLine(DataProcessLogWriter dataProcessLogWriter, int line) {
        for (int i = 0; i <line; i++) {
            LogEvent logEvent = new LogEvent();
            logEvent.setStatus("0");
            logEvent.setTimeStamp(1500012313);
            logEvent.setPath("ftp://s100:/2018/01/09");
            dataProcessLogWriter.writeEvent(logEvent);
        }
    }

}
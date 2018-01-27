package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Test;

/**
 * Created by Administrator on 2018-1-26.
 */
public class AbstractLogWriteTest {
    @Test
    public void prepare() throws Exception {

    }

    @Test
    public void writeEvent() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        String queueID = "13";
        conf.setProcessLogDir("E:\\getProcessLogDir\\process");
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        //writerOneLine(dataProcessLogWriter);
        //生成两个日志文件，一个30w行，一个1行，测试日志文件自动生成
        //writeMultiLine(dataProcessLogWriter, 300001);
        //测试往已经存在的空日志文件中写日志
        for (int i = 0; i <2 ; i++) {
            writeMultiLine(dataProcessLogWriter, 300000);
        }
    }

    private void writerOneLine(DataProcessLogWriter dataProcessLogWriter) {
        LogEvent logEvent = new LogEvent();
        logEvent.setCount(1);
        logEvent.setStatus("0");
        logEvent.setTimeStamp("2018-01-02");
        logEvent.setUrl("ftp://s100:/2018/01/09");
        dataProcessLogWriter.writeEvent(logEvent);
    }

    private void writeMultiLine(DataProcessLogWriter dataProcessLogWriter, int line) {
        for (int i = 1; i <= line; i++) {
            LogEvent logEvent = new LogEvent();
            logEvent.setStatus("0");
            logEvent.setTimeStamp("2018-01-02");
            logEvent.setUrl("ftp://s100:/2018/01/09");
            dataProcessLogWriter.writeEvent(logEvent);
        }
    }

}
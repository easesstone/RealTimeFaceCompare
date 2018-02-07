package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Test;

import java.util.Random;


/**
 * AbstractLogWriteTest 类测试方法
 * 测试用例：
 * 1.往日志中写入一行数据（测试能否正常些日志）
 * 2.写入logsize数量的数据（测试能够正常生成新的日志文件）
 * 3.写入logsize+1条数据（测试新的日志文件写不否正常）
 * 4.循环写入大批量数据（同2，相当于多次测试）
 * 5.在4的基础上再次写入大批量数据（之前存在再次写日志时不能生成新的日志文件）
 * 6.删除日志目录下所有日志文件，重新写入大批量数据（测试代码获取日志最后一行其中一个分支是否正确）
 * 7.删除日志目录，写日志（测试是否能够自动创建目录）
 * 8.当0000000000.log中存在小于logsize行数的日志情况下，加入空格，再次运行程序（测试写日志方法能否处理日志文件末尾的多个空行）
 * 彭聪
 */
public class AbstractLogWriteTest {
    @Test
    public void logNameUpdate() throws Exception {
    }

    @Test
    public void getLastLine() throws Exception {
    }

    @Test
    public void getLastCount() throws Exception {
    }

    @Test
    public void action() throws Exception {
    }

    @Test
    public void countCheckAndWrite() throws Exception {
    }

    @Test
    /**
     * 测试prepare方法，能否自动创建目录
     */
    public void prepare() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        String queueID = "25";
        conf.setProcessLogDir("E:/getProcessLogDir/process");
        conf.setReceiveLogDir("E:/getProcessLogDir/receive");
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        dataProcessLogWriter.prepare();
        DataReceiveLogWriter dataReceiveLogWriter = new DataReceiveLogWriter(conf, queueID);
        dataReceiveLogWriter.prepare();

    }

    @Test
    /**
     * 写日志测试方法,同时向接收和处理日志中写日志
     */
    public void writevent() {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        String queueID = "25";
        conf.setProcessLogDir("E:/getProcessLogDir/process/");
        conf.setReceiveLogDir("E:/getProcessLogDir/receive/");
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        DataReceiveLogWriter dataReceiveLogWriter = new DataReceiveLogWriter(conf, queueID);
        for (int i = 0; i < 10; i++) {
            writeMultiLine(dataProcessLogWriter, conf.getLogSize());
            writeMultiLine(dataReceiveLogWriter, conf.getLogSize());
        }
    }

    /**
     * 往日志文件中写入一行日志
     *
     * @param abstractLogWrite
     */
    private void writerOneLine(AbstractLogWrite abstractLogWrite) {
        LogEvent logEvent = new LogEvent();
        logEvent.setStatus("0");
        logEvent.setTimeStamp(1500012313);
        logEvent.setPath("ftp://s100:/2018/01/09");
        abstractLogWrite.countCheckAndWrite(logEvent);
    }

    /**
     * 往日志文件中写入多行数据,根据对象类型日入不同的目录下，DataProcessLogWriter状态码采用随机的方式生成
     *
     * @param abstractLogWrite 类对象
     * @param line             写入日志的行数
     */
    private void writeMultiLine(AbstractLogWrite abstractLogWrite, int line) {
        if (abstractLogWrite instanceof DataReceiveLogWriter) {
            for (int i = 0; i < line; i++) {
                LogEvent logEvent = new LogEvent();
                logEvent.setStatus("0");
                logEvent.setTimeStamp(1500012313);
                logEvent.setPath("ftp://s100:/2018/01/09");
                abstractLogWrite.countCheckAndWrite(logEvent);
            }
        } else if (abstractLogWrite instanceof DataProcessLogWriter) {
            Random rand = new Random();
            for (int i = 0; i < line; i++) {
                String status = String.valueOf(rand.nextInt(2));
                LogEvent logEvent = new LogEvent();
                logEvent.setStatus(status);
                logEvent.setTimeStamp(1500012313);
                logEvent.setPath("ftp://s100:/2018/01/09");
                abstractLogWrite.countCheckAndWrite(logEvent);
            }
        } else {
            System.out.println("类型错误");
        }
    }

}
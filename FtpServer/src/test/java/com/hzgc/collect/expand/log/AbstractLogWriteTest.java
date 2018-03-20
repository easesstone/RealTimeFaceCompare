package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.merge.MergeUtil;
import com.hzgc.collect.expand.util.JSONHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * AbstractLogWriteTest 类测试方法
 * 测试用例：
 * 1.往日志中写入一行数据（测试能否正常些日志）
 * 2.写入logsize数量的数据（测试能够正常生成新的日志文件）
 * 3.写入logsize+1条数据（测试新的日志文件写不否正常）
 * 4.循环写入大批量数据，每批次大小为logsize（同2，相当于多次测试）
 * 5.在4的基础上再次写入大批量数据（之前存在再次写日志时不能生成新的日志文件）
 * 6.删除日志目录下所有日志文件，重新写入大批量数据（测试代码获取日志最后一行其中一个分支是否正确）
 * 7.删除日志目录，写日志（测试是否能够自动创建目录）
 * 8.当0000000000.log中存在小于logsize行数的日志情况下，加入空格，再次运行程序（测试写日志方法能否处理日志文件末尾的多个空行）
 * 9.多次循环随机的写入一批数据
 */
public class AbstractLogWriteTest {

    @Test
    /**
     * 测试prepare方法，能否自动创建目录
     */
    public void prepare() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        Random random = new Random();
        String queueID = String.
                valueOf(random.nextInt(conf.getReceiveNumber()));
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        dataProcessLogWriter.prepare();
        Path receivePath = Paths.get(conf.getProcessLogDir() + "/process-" + queueID);
        Assert.assertTrue("directory not create successful", Files.exists(receivePath));
        DataReceiveLogWriter dataReceiveLogWriter = new DataReceiveLogWriter(conf, queueID);
        dataReceiveLogWriter.prepare();
        Path processPath = Paths.get(conf.getProcessLogDir() + "/process-" + queueID);
        Assert.assertTrue("directory not create successful", Files.exists(processPath));
    }

    @Test
    /**
     * 写日志测试方法,同时向接收和处理日志中写日志
     * all the test unit run from here
     */
    public void writevent() throws Exception {
        CommonConf conf = new CommonConf();
        conf.setLogName(conf.getLogName());
        Random random = new Random();
        String queueID = String.
                valueOf(random.nextInt(conf.getReceiveNumber()));
        //delete directory and files before run the code
        MergeUtil mergeUtil = new MergeUtil();
        mergeUtil.deleteFile(conf.getProcessLogDir());
        mergeUtil.deleteFile(conf.getReceiveLogDir());
        DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(conf, queueID);
        DataReceiveLogWriter dataReceiveLogWriter = new DataReceiveLogWriter(conf, queueID);

        //写入一行日志
        writeMultiLine(dataProcessLogWriter, 1);
        long lastLine = dataProcessLogWriter.getLastCount() - 1;
        Assert.assertTrue("write process log failed", lastLine == 1);
        writeMultiLine(dataReceiveLogWriter, 1);
        long lastLine2 = dataReceiveLogWriter.getLastCount() - 1;
        Assert.assertTrue("write process log failed", lastLine == 1);

        //delete directory and files before run the code
        mergeUtil.deleteFile(conf.getProcessLogDir());
        mergeUtil.deleteFile(conf.getReceiveLogDir());
        //写入logsize行数据
        dataProcessLogWriter.prepare();
        writeMultiLine(dataProcessLogWriter, conf.getLogSize());
        dataReceiveLogWriter.prepare();
        writeMultiLine(dataReceiveLogWriter, conf.getLogSize());
        long lastLine3 = dataProcessLogWriter.getLastCount() - 1;
        Assert.assertTrue("write process log failed", lastLine3 == conf.getLogSize());
        long lastLine4 = dataReceiveLogWriter.getLastCount() - 1;
        Assert.assertTrue("write receive log failed", lastLine4 == conf.getLogSize());

        //写入logSize+1行数据
        //delete directory and files before run the code
        mergeUtil.deleteFile(conf.getProcessLogDir());
        mergeUtil.deleteFile(conf.getReceiveLogDir());
        dataProcessLogWriter.prepare();
        dataReceiveLogWriter.prepare();
        writeMultiLine(dataProcessLogWriter, conf.getLogSize() + 1);
        long lastLine5 = dataProcessLogWriter.getLastCount() - 1;
        Assert.assertTrue("write process log failed", lastLine5 == conf.getLogSize() + 1);
        dataReceiveLogWriter.prepare();
        writeMultiLine(dataReceiveLogWriter, conf.getLogSize() + 1);
        long lastLine6 = dataReceiveLogWriter.getLastCount() - 1;
        Assert.assertTrue("write receive log failed", lastLine6 == conf.getLogSize() + 1);

        //写入随机数量的日志，多次运行
        mergeUtil.deleteFile(conf.getProcessLogDir());
        mergeUtil.deleteFile(conf.getReceiveLogDir());
        dataProcessLogWriter.prepare();
        dataReceiveLogWriter.prepare();
        int loops = random.nextInt(100);
        int totalLines = 0;
        for (int i = 0; i < loops; i++) {
            Random random1 = new Random();
            int lines = random1.nextInt(100);
            writeMultiLine(dataProcessLogWriter, lines);
            writeMultiLine(dataReceiveLogWriter, lines);
            totalLines += lines;
        }
        long lastLine7 = dataProcessLogWriter.getLastCount() - 1;
        long lastLine8 = dataReceiveLogWriter.getLastCount() - 1;
        Assert.assertTrue("write log failed", lastLine7 == totalLines && lastLine8 == totalLines);

        //当0000000000.log中存在小于logsize行数的日志情况下，加入空格，再次运行程序（测试写日志方法能否处理日志文件末尾的多个空行）
        FileWriter fw = new FileWriter(dataReceiveLogWriter.currentFile, true);
        for (int i = 0; i < 10; i++) {
            fw.write(System.getProperty("line.separator"));
            fw.flush();
        }
        int moreLine = random.nextInt(100);
        writeMultiLine(dataReceiveLogWriter, moreLine);
        long lastLine9 = dataReceiveLogWriter.getLastCount() - 1;
        Assert.assertTrue("get lastLineCount failed", lastLine9 == totalLines + moreLine);

        //循环写入大量数据，完成后退出，再次执行下面代码
       /* for (int i = 0; i < 10; i++) {
            writeMultiLine(dataProcessLogWriter, conf.getLogSize());
            writeMultiLine(dataReceiveLogWriter, conf.getLogSize());
        }*/
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
                logEvent.setFtpPath("ftp://s100:/2018/01/09");
                abstractLogWrite.countCheckAndWrite(logEvent);
            }
        } else if (abstractLogWrite instanceof DataProcessLogWriter) {
            Random rand = new Random();
            for (int i = 0; i < line; i++) {
                String status = String.valueOf(rand.nextInt(2));
                LogEvent logEvent = new LogEvent();
                logEvent.setStatus(status);
                logEvent.setTimeStamp(1500012313);
                logEvent.setFtpPath("ftp://s100:/2018/01/09");
                abstractLogWrite.countCheckAndWrite(logEvent);
            }
        } else {
            System.out.println("类型错误");
        }
    }

}
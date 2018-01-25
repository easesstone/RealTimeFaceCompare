package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.JsonHelper;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * 此对象为数据处理写日志对象的实例
 */
public class DataProcessLogWriter extends AbstractLogWrite {
    private static final Logger LOG = Logger.getLogger(DataProcessLogWriter.class);
    /**
     * 处理日志文件大小
     */
    private int processLogSize;

    /**
     * 处理日志文件名称
     */
    private String processLogName;

    /**
     * 当前队列序号,默认从1开始
     */
    private long count;

    private String currentDir;

    private String currentFile;


    DataProcessLogWriter(CommonConf conf, String queueID) {
        super(queueID);
        this.processLogSize = conf.getProcessLogSize();
        this.processLogName = conf.getProcessLogName();
        this.currentDir = conf.getProcessLogDir() + "/" + "process-" + super.queueID + "/";
        this.currentFile = this.currentDir + processLogName;
        LOG.info("Init DataProcessLogWriter successfull [" + queueID + ":" + this.queueID
                + ", count:" + count
                + ", processLogName:" + this.processLogName
                + ", processLogSize:" + this.processLogSize
                + ", currentFile:" + this.currentFile + "]");
        this.prepare();
    }

    @Override
    public void writeEvent(LogEvent event) {
        if (this.count % processLogSize == 0) {
            File oldFile = new File(this.currentFile);
            File newFile = new File(currentDir + logNameUpdate(this.processLogName, count));
            oldFile.renameTo(newFile);
            action(event);
        } else {
            action(event);
        }

    }

    @Override
    protected void prepare() {
        File logDir = new File(this.currentDir);
        if (!logDir.exists()) {
            logDir.mkdirs();
            this.count = 1;
        } else if (null == logDir.list()) {
            this.count = 1;
        } else {
            File logFile = new File(this.currentFile);
            if (!logFile.exists()) {
                this.count = getLastCount(getLastLogFile(this.currentDir));
            } else {
                this.count = getLastCount(this.currentFile);
            }
        }
    }

    private void action(LogEvent event) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.currentFile, true);
            fw.write(JsonHelper.toJson(event));
            fw.write(newLine);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.count++;
        }
    }
}

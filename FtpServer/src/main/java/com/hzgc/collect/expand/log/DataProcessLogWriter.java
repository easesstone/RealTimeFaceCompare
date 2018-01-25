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
     * 处理队列日志目录
     */
    private String processLogDir;

    /**
     * 当前队列序号,默认从1开始
     */
    private long count;

    private String newLine;

    private String currentDir;

    private String currentFile;


    DataProcessLogWriter(CommonConf conf, String queueID) {
        super(conf, queueID);
        this.processLogSize = conf.getProcessLogSize();
        this.processLogName = conf.getProcessLogName();
        this.processLogDir = conf.getProcessLogDir();
        this.newLine = System.getProperty("line.separator");
        this.currentDir = this.processLogDir + "/" + "process-" + super.queueID + "/";
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
            FileWriter fw = null;
            File oldFile = new File(this.currentFile);
            File newFile = new File(currentDir + logNameUpdate(this.processLogName, count));
            oldFile.renameTo(newFile);
            try {
                oldFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        } else {

        }

    }

    private void action(LogEvent event) {
        FileWriter fw;
        try {
            fw = new FileWriter(this.currentFile, true);
            fw.write(JsonHelper.toJson(event));
            fw.write(newLine);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        count++;
    }

    public int getProcessLogSize() {
        return processLogSize;
    }

    public void setProcessLogSize(int processLogSize) {
        this.processLogSize = processLogSize;
    }

    public String getProcessLogName() {
        return processLogName;
    }

    public void setProcessLogName(String processLogName) {
        this.processLogName = processLogName;
    }

    public String getProcessLogDir() {
        return processLogDir;
    }

    public void setProcessLogDir(String processLogDir) {
        this.processLogDir = processLogDir;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}

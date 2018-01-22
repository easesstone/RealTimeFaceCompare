package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
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

    private String currentFile;

    /**
     * 当前队列序号
     */
    private long count;

    private String newLine;

    DataProcessLogWriter(CommonConf conf, String queueID, long count) {
        super(conf, queueID);
        this.processLogSize = conf.getProcessLogSize();
        this.processLogName = conf.getProcessLogName();
        this.processLogDir = conf.getProcessLogDir();
        this.count = count;
        this.newLine = System.getProperty("line.separator");
        this.currentFile = this.processLogDir + "/" + "process-" + super.queueID + "/" + processLogName;
        LOG.info("Init DataProcessLogWriter successfull [" + queueID + ":" + this.queueID
                + ", count:" + count
                + ", processLogName:" + this.processLogName
                + ", processLogSize:" + this.processLogSize
                + ", currentFile:" + this.currentFile + "]");
        this.prepare();
    }

    @Override
    public void writeEvent(LogEvent event) {
        try {
            FileWriter fw = new FileWriter(this.currentFile, true);
            fw.write("");
            fw.write(newLine);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void prepare() {
        if (count == 0) {
            File file = new File(this.currentFile);
            try {
                if (!file.exists()) {
                    boolean status = file.createNewFile();
                    LOG.info("Current count is " + count + ", create log file:" + this.currentFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

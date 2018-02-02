package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.JSONHelper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 此对象为数据处理写日志对象的实例
 *
 * @author Zhaozhe
 */
public class DataProcessLogWriter extends AbstractLogWrite {
    public DataProcessLogWriter(CommonConf conf, String queueID) {
        super(conf, queueID, DataProcessLogWriter.class);
        super.currentDir = conf.getProcessLogDir() + "/" + "process-" + super.queueID + "/";
        super.currentFile = super.currentDir + super.logName;
        super.prepare();
        LOG.info("Init DataProcessLogWriter successful [queueID:" + super.queueID
                + ", count:" + count
                + ", LogName:" + super.logName
                + ", LogSize:" + super.logSize
                + ", currentFile:" + super.currentFile + "]");
    }

    /**
     * 通过文件锁lock的方式实现文件互斥读写
     *
     * @param errorPath 错误日志文件路径
     * @param event     错误日志
     */
    void errorLogWrite(String errorPath, LogEvent event) {
        Path errorLogDir = Paths.get(errorPath);
        Path errorLogFile = Paths.get(errorPath, "error.log");
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        try {
            if (!Files.isDirectory(errorLogDir)) {
                Files.createDirectories(errorLogDir);
            } else {
                if (!Files.exists(errorLogFile)) {
                    Files.createFile(errorLogFile);
                }
            }
            randomAccessFile = new RandomAccessFile(errorLogFile.toFile(), "rw");
            fileChannel = randomAccessFile.getChannel();
            while (true) {
                try {
                    fileLock = fileChannel.tryLock();
                    if (fileLock.isValid()) {
                        break;
                    }
                } catch (Exception e) {
                    LOG.info("other thread is operating this file,the current thread is: " + Thread.currentThread().getName() + " sleep 1 second and try it again");
                    try {
                        //读写线程通过设置不同的睡眠时间解决死锁的问题
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            byte[] bytes = JSONHelper.toJson(event).getBytes("utf-8");
            long length = randomAccessFile.length();
            long position = length - 1;
            if (length != 0L) {
                randomAccessFile.seek(position);
            } else {
                randomAccessFile.seek(0L);
            }
            randomAccessFile.write(bytes);
            randomAccessFile.write(System.getProperty("line.separator").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileLock != null) {
                    fileLock.release();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


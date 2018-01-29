package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Objects;

/**
 * 此对象为抽象类，实现了LogWriter接口，并在其中定义了如下成员：
 * 1.子类的公共字段
 * 2.有参构造器，子类可通过ReceiverConf进行实例化
 * 3.无参构造器私有化，即不能通过无参构造器进行实例化
 * 如果需要实现LogWriter定义的功能需要继承AbstractLogGroupWrite
 */
abstract class AbstractLogWrite implements LogWriter {
    static Logger LOG;
    /**
     * 当前队列ID
     */
    String queueID;

    /**
     * 处理日志文件大小
     */
    int logSize;

    /**
     * 处理日志文件名称
     */
    String logName;

    /**
     * 当前队列序号,默认从1开始
     */
    long count;

    /**
     * 当前日志所在目录
     */
    String currentDir;

    /**
     * 当前日志绝对路径
     */
    String currentFile;

    /**
     * 系统换行符
     */
    private String newLine;

    /**
     * 构造LogWriter的公共字段
     *
     * @param conf    全局配置
     * @param queueID 当前队列ID
     * @param clz     当前类Class
     */
    AbstractLogWrite(CommonConf conf, String queueID, Class clz) {
        LOG = Logger.getLogger(clz);
        this.queueID = queueID;
        this.newLine = System.getProperty("line.separator");
        this.logSize = conf.getLogSize();
        this.logName = conf.getLogName();
    }

    /**
     * 前置方法，通过此方法，可以初始化当前队列的序号（count）、
     * 如果当前队列之前有日志记录，则找到最后一个序号
     * 如果未找到序号或者是第一次创建此队列，序号（count）为1
     */
    void prepare() {
        File logDir = new File(this.currentDir);
        if (!logDir.exists()) {
            logDir.mkdirs();
            this.count = 1;
        } else if (null == logDir.list()) {
            this.count = 1;
        } else {
            File logFile = new File(this.currentFile);
            if (!logFile.exists()) {
                this.count = getLastCount(this.currentDir + getLastLogFile(this.currentDir));
            } else {
                this.count = getLastCount(this.currentFile);
            }
        }
    }

    /**
     * 当默认日志文件写入的日志个数大于配置的个数时,
     * 需要重新再写入新的日志,之前的默认日志文件名称里会包含最后一行日志的count值
     * 此方法用来生成这个文件名称
     *
     * @param defaultName 默认写入的日志文件名称
     * @param count       要标记的count值
     * @return 最终合并的文件名称
     */
    private String logNameUpdate(String defaultName, long count) {
        char[] oldChar = defaultName.toCharArray();
        char[] content = (count + "").toCharArray();
        for (int i = 0; i < content.length; i++) {
            oldChar[oldChar.length - 1 - i] = content[content.length - 1 - i];
        }
        return new String(oldChar);
    }

    /**
     * 获取当前队列的最后一行日志的序号
     *
     * @param currentLogFile 当前队列的日志文件绝对路径
     * @return 当前队列的序号
     */
    private long getLastCount(String currentLogFile) {
        try {
            System.out.println("***********:"+currentLogFile);
            RandomAccessFile raf = new RandomAccessFile(currentLogFile, "r");
            long length = raf.length();
            long position = length - 1;
            if (position == -1) {
                return 0;
            } else {
                while (position > 0) {
                    position--;
                    raf.seek(position);
                    if (raf.readByte() == '\n') {
                        break;
                    }
                }
                if (position == 0) {
                    raf.seek(0);
                }
                byte[] bytes = new byte[(int) (length - position)];
                raf.read(bytes);
                String json = new String(bytes);
                if (json.trim().length() > 0) {
                    LogEvent event = JSONHelper.toObject(json.trim(), LogEvent.class);
                    return event.getCount();
                } else {
                    return 1;
                }

            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 获取最大序号（count）所在的日志文件的绝对路径
     *
     * @param currentDir 当前队列所在的目录
     * @return 日志文件的绝对路径
     */
    private String getLastLogFile(String currentDir) {
        File file = new File(currentDir);
        String[] fileArray = file.list();
        if (fileArray != null&&fileArray.length>0) {
            Arrays.sort(fileArray);
            if (fileArray.length == 1 && Objects.equals(fileArray[0], this.currentFile)) {
                return this.currentFile;
            } else {
                return fileArray[fileArray.length - 1];
            }
        } else {
            return this.currentFile;
        }
    }

    /**
     * 写日志操作，使用追加的方法
     *
     * @param event 封装的日志信息
     */
    private void action(LogEvent event) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.currentFile, true);
            event.setCount(this.count);
            fw.write(JSONHelper.toJson(event));
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

    @Override
    public void writeEvent(LogEvent event) {
        if (this.count % this.logSize == 0) {
            File oldFile = new File(this.currentFile);
            File newFile = new File(currentDir + logNameUpdate(this.logName, count));
            // TODO: 2018-1-29
            action(event);
            oldFile.renameTo(newFile);
        } else {
            action(event);
        }

    }
}

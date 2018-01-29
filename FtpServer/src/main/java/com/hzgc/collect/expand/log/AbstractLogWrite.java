package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;

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
    @Override
    public void prepare() {
        File logDir = new File(this.currentDir);
        if (!logDir.exists() || logDir.list() == null) {
            logDir.mkdirs();
            this.count = 1;
        } else if (logDir.length() == 0) {
            this.count = 1;
        } else {
            this.count = getLastCount();
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
    @Override
    public String logNameUpdate(String defaultName, long count) {
        char[] oldChar = defaultName.toCharArray();
        char[] content = (count + "").toCharArray();
        for (int i = 0; i < content.length; i++) {
            oldChar[oldChar.length - 1 - i] = content[content.length - 1 - i];
        }
        return new String(oldChar);
    }

    /**
     * 获取指定文件的最后一行
     * @param fileName 指定文件名称
     * @return 最后一行名称
     */
    @Override
    public String getLastLine(String fileName) {
        try {
            String tempFile = this.currentDir + fileName;
            RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
            LOG.info("Start get last line from " + tempFile);
            long length = raf.length();
            long position = length - 1;
            raf.seek(position);
            if (position != -1) {
                while (position >= 0) {
                    if (raf.read() != '\n') {
                        break;
                    }
                    if (position == 0) {
                        raf.seek(position);
                        break;
                    } else {
                        position--;
                        raf.seek(position);
                    }
                }
                System.out.println(position);
                if (position >= 0) {
                    while (position >= 0) {
                        if (position == 0) {
                            raf.seek(position);
                            break;
                        } else {
                            position--;
                            raf.seek(position);
                            if (raf.read() == '\n') {
                                break;
                            }
                        }
                    }
                }
                String line = raf.readLine();
                if (line != null) {
                    LOG.info("Last line from " + tempFile + " is:[" + line + "]");
                    return line;
                } else {
                    LOG.warn("Last line from " + tempFile + " is:[" + null + "], maybe this queue never receives data");
                    return "";
                }
            } else {
                LOG.warn("Last line from " + tempFile + " is:[" + null + "], maybe this queue never receives data");
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取最大序号（count）所在的日志文件的绝对路径
     *
     * @return 日志文件的绝对路径
     */
    @Override
    public long getLastCount() {
        File currentDir = new File(this.currentDir);
        String[] dirList = currentDir.list();
        File defaultFile = new File(this.currentFile);
        if (dirList != null && dirList.length > 0) {
            if (defaultFile.exists()) {
                String countLine = getLastLine(defaultFile.getName());
                if (countLine.length() == 0) {
                    LOG.info("Get count from " + this.currentFile
                            + ", but the file content is null, so queue id is "
                            + this.queueID + ", count is 1");
                    return 1;
                } else {
                    LogEvent event = JSONHelper.toObject(countLine.trim(), LogEvent.class);
                    LOG.info("Get count from " + this.currentFile + "queue id is "
                            + this.queueID + ", count is " + event.getCount());
                    return event.getCount();
                }
            } else {
                LOG.info("Default log file "
                        + this.currentFile
                        + "is not exists, start check other log file and sort by file name");
                Arrays.sort(dirList);
                LOG.info("Sort result is " + Arrays.toString(dirList));
                String countFile = dirList[dirList.length - 1];
                String countLine = getLastLine(countFile);
                LogEvent event = JSONHelper.toObject(countLine.trim(), LogEvent.class);
                LOG.info("Get count from " + countFile
                        + ", queue is is " + this.queueID
                        + ", count is " + event.getCount());
                return event.getCount();
            }
        } else {
            LOG.info("Directory " + this.currentDir + " is not exists or empty, so queue id is "
                    + this.queueID + ", count is 1");
            return 1;
        }
    }

    /**
     * 写日志操作，使用追加的方法
     *
     * @param event 封装的日志信息
     */
    @Override
    public void action(LogEvent event) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(this.currentFile, true);
            event.setCount(++this.count);
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
        }
    }

    @Override
    public void countCheckAndWrite(LogEvent event) {
        if (this.count % this.logSize == 0) {
            File oldFile = new File(this.currentFile);
            File newFile = new File(currentDir + logNameUpdate(this.logName, count));
            action(event);
            oldFile.renameTo(newFile);
        } else {
            action(event);
        }

    }
}

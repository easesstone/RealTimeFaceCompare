package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * 此对象为抽象类，实现了LogWriter接口，并在其中定义了如下成员：
 * 1.子类的公共字段
 * 2.有参构造器，子类可通过ReceiverConf进行实例化
 * 3.无参构造器私有化，即不能通过无参构造器进行实例化
 * 如果需要实现LogWriter定义的功能需要继承AbstractLogGroupWrite
 */
abstract class AbstractLogWrite implements LogWriter {
    /**
     * 当前队列ID
     */
    protected String queueID;

    /**
     * 私有无参构造器
     */
    private AbstractLogWrite() {

    }

    /**
     * @param conf    ReceiverConf对象
     * @param queueID 当前队列ID
     */
    AbstractLogWrite(CommonConf conf, String queueID) {
    }

    abstract protected void prepare();

    /**
     * 当默认日志文件写入的日志个数大于配置的个数时,
     * 需要重新再写入新的日志,之前的默认日志文件名称里会包含最后一行日志的count值
     * 此方法用来生成这个文件名称
     *
     * @param defaultName 默认写入的日志文件名称
     * @param count 要标记的count值
     * @return 最终合并的文件名称
     */
    String logNameUpdate(String defaultName, long count) {
        char[] oldChar = defaultName.toCharArray();
        char[] content = (count + "").toCharArray();
        for (int i = 0; i < content.length; i++) {
            oldChar[oldChar.length - 1 - i] = content[content.length - 1 - i];
        }
        return new String(oldChar);
    }

    long getLastCount(String currentLogFile) {
        try {
            LineNumberReader numberReader = new LineNumberReader(new FileReader(currentLogFile));
//            numberReader.skip()
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
    public String getQueueID() {
        return queueID;
    }

    public void setQueueID(String queueID) {
        this.queueID = queueID;
    }
}

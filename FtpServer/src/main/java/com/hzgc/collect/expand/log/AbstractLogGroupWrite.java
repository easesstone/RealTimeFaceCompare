package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.RecvicerConf;

/**
 * 此对象为抽象类，实现了LogWriter接口，并在其中定义了如下成员：
 * 1.子类的公共字段
 * 2.有参构造器，子类可通过ReceiverConf进行实例化
 * 3.无参构造器私有化，即不能通过无参构造器进行实例化
 * 如果需要实现LogWriter定义的功能需要继承AbstractLogGroupWrite
 */
public abstract class AbstractLogGroupWrite implements LogWriter {
    /**
     * 日志文件名称
     */
    public String logName;

    /**
     * 日志目录
     */
    public String logPath;

    /**
     * 日志大小，以个数计算
     */
    public String logSize;

    /**
     * 私有无参构造器
     */
    private AbstractLogGroupWrite() {

    }

    /**
     * 通过ReceiverConf来实例化对应属性
     *
     * @param conf ReceiverConf对象
     */
    AbstractLogGroupWrite(RecvicerConf conf) {
    }
}

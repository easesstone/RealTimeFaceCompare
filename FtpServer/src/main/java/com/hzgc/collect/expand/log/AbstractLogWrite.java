package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

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
    private String queueID;

    /**
     * 私有无参构造器
     */
    private AbstractLogWrite() {

    }

    /**
     *
     * @param conf ReceiverConf对象
     * @param queueID 当前队列ID
     */
    AbstractLogWrite(CommonConf conf, String queueID) {
    }

    public String getQueueID() {
        return queueID;
    }

    public void  setQueueID(String queueID) {
        this.queueID = queueID;
    }
}

package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

/**
 * 此对象为数据接收写日志对象的实例
 * @author Pengcong
 */
public class DataReceiveLogWriter extends AbstractLogWrite {

    public DataReceiveLogWriter(CommonConf conf, String queueID) {
        super(conf, queueID, DataProcessLogWriter.class);
        super.currentDir = conf.getProcessLogDir() + "/" + "receive-" + super.queueID + "/";
        super.currentFile = super.currentDir + super.logName;
        super.prepare();
        LOG.info("Init DataReceiveLogWriter successful [" + queueID + ":" + super.queueID
                + ", count:" + count
                + ", LogName:" + super.logName
                + ", LogSize:" + super.logSize
                + ", currentFile:" + super.currentFile + "]");
    }
}

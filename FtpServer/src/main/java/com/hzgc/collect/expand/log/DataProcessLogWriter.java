package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

/**
 * 此对象为数据处理写日志对象的实例
 * @author Zhaozhe
 */
public class DataProcessLogWriter extends AbstractLogWrite {
    public DataProcessLogWriter(CommonConf conf, String queueID) {
        super(conf, queueID, DataProcessLogWriter.class);
        super.currentDir = conf.getProcessLogDir() + "/" + "process-" + super.queueID + "/";
        super.currentFile = super.currentDir + super.logName;
        super.prepare();
        LOG.info("Init DataProcessLogWriter successfull [queueID:" + super.queueID
                + ", count:" + count
                + ", LogName:" + super.logName
                + ", LogSize:" + super.logSize
                + ", currentFile:" + super.currentFile + "]");
    }

}

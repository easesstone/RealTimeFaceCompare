package com.hzgc.collect.expand.meger;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 恢复未处理的数据(曹大报)
 */
public class RcoverNotProData {
    private Logger LOG = Logger.getLogger(RcoverNotProData.class);
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");


    public boolean recoverNotProData(CommonConf commonConf) {
        FileUtil fileUtil = new FileUtil();
        LogEvent logEvent = new LogEvent();
        String processLogDir = commonConf.getProcessLogDir();

        FileFactory fileFactory = new FileFactory(processLogDir);
        List<String> processFiles = fileFactory.getAllProcessFiles();
        //标记恢复数据是否成功，默认false
        boolean recoverSuccess = false;

        //判断process根目录下是否有文件
        if (processFiles != null && processFiles.size() != 0) {
            for (String processFile : processFiles) {
                //获取receive绝对路径
                String receiveFile = fileUtil.getRecFileFromProFile(processFile);
                //判断对应receive文件是否存在，存在则合并，不存在则删除
                if (fileUtil.isFileExist(receiveFile)) {
                    //获取队列ID
                    String queueID = processFile.substring(processFile.lastIndexOf("-") + 1, processFile.lastIndexOf("/"));
                    DataProcessLogWriter dataProcessLogWriter = new DataProcessLogWriter(commonConf, queueID);
                    RowsListFactory rowsListFactory = new RowsListFactory(processFile, receiveFile);
                    //获取未处理的数据
                    List<String> notProRows = rowsListFactory.getNotProRows();
                    for (int j = 0; j < notProRows.size(); j++) {
                        String row = notProRows.get(j);
                        //获取未处理数据的ftpUrl
                        LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                        String ftpUrl = event.getPath();
                        //获取该条数据的序列号
                        long count = event.getCount();
                        FaceObject faceObject = GetFaceObject.getFaceObject(row);
                        if (faceObject != null) {
                            SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                            sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                            boolean success = sendDataToKafka.isSuccessToKafka();
                            if (j == 0 && !success) {
                                LOG.warn("first data send to Kafka failure");
                                return false;
                            } else {
                                LOG.info("Write log queueID is" + queueID + "" + processFile);
                                //向对应的processfile中写入日志
                                logEvent.setPath(ftpUrl);
                                if (success) {
                                    logEvent.setStatus("0");
                                } else {
                                    logEvent.setStatus("1");
                                }
                                logEvent.setCount(count);
                                logEvent.setTimeStamp(Long.valueOf(SDF.format(new Date())));
                                dataProcessLogWriter.writeEvent(logEvent);
                                recoverSuccess = true;
                            }
                        }
                    }
                } else {
                    //对应receive 文件不存在，删除对应文件
                    boolean deleteFile = fileUtil.deleteFile(processFile);
                    if (!deleteFile) {
                        LOG.warn("delete file " + processFile + "failure,please check it！");
                    }
                    recoverSuccess = true;
                }
            }
            return recoverSuccess;
        } else {
            LOG.info("The path of " + processLogDir + "is Nothing");
            return true;
        }
    }
}

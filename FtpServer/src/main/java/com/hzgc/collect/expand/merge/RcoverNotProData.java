package com.hzgc.collect.expand.merge;

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
 *
 *  * 整体流程：
 * 1，根据CommonConf获取process根路径processLogDir；
 * 2，遍历process 目录中的所有文件，得到日记文件的一个绝对路径，放入一个List中，processFiles；
 *     判断 processFiles 是否为空(process 目录下是否有文件)；
 *         1) processFiles不为空，遍历获取 processFiles 中每一个元素(process文件的绝对路径)，例如：/opt/logdata/process/p-0/000003000000.log；
 *
 *             根据process文件获取receive文件路径，判断对应receive文件(/opt/logdata/receive/r-0/000003000000.log)是否存在；
 *             存在：
 *                 1，读取process文件和receive文件，合并到一个List 里面(rowsListFactory)；
 *                 2，排序，对比，获取有序的数据集合 notProRows；
 *                 3，遍历 notProRows 每一条数据，提取特征获取faceObject发送Kafka；
 *                     根据发送kafka是否成功分别写入不同日志文件中；
 *                     第一条数据发送kafka失败；
 *                         结束循环返回false；
 *             不存在：
 *                  把/opt/logdata/process/p-0/000003000000.log 文件移动到success目录下，跳过这层循环。
 *         2）processFiles 为空
 *             结束循环；
 * 3，结束
 */
public class RcoverNotProData {
    private Logger LOG = Logger.getLogger(RcoverNotProData.class);
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");


    public boolean RecoverNotProData(CommonConf commonConf) {
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
                //判断对应receive文件是否存在，存在则合并，不存在则移动位置
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
                                //向对应的processFile中写入日志
                                logEvent.setPath(ftpUrl);
                                logEvent.setCount(count);
                                logEvent.setTimeStamp(Long.valueOf(SDF.format(new Date())));
                                if (success) {
                                    logEvent.setStatus("0");
                                    LOG.info("Send to Kafka success,write log to processFile :" + processFile);
                                    fileUtil.writeMergeFile(logEvent, processFile);
                                } else {
                                    //发送Kafka失败
                                    logEvent.setStatus("1");
                                    String errorFilePath = fileUtil.getErrFileFromProFile();
                                    LOG.warn("Send to Kafka failure ,write log to errorLogFile :");
                                    dataProcessLogWriter.errorLogWrite(errorFilePath, logEvent);
                                }
                                recoverSuccess = true;
                            }
                        }
                    }
                } else {
                    //对应receive 文件不存在，将process文件移动到success目录下
                    LOG.info("Can't find receiveFile,move processFile To SuccessDir");
                    String successFilePath = fileUtil.getSuccessFilePath(processFile);
                    fileUtil.moveFile(processFile, successFilePath);
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

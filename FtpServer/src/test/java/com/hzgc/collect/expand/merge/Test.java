package com.hzgc.collect.expand.merge;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import com.hzgc.collect.expand.util.JSONHelper;
import com.hzgc.jni.NativeFunction;

import java.text.ParseException;
import java.util.List;


public class Test {
    static {
        NativeFunction.init();
    }

    public static void main(String[] args) throws ParseException {
        MergeUtil mergeUtil = new MergeUtil();
        CommonConf conf = new CommonConf();
        String processLogDir = conf.getProcessLogDir();
        List<String> processLogPaths = mergeUtil.listAllFileAbsPath(processLogDir);
        //获取正在写的日志队列文件
        String writingLogFile = conf.getLogName();
        //获取merge/error目录：/opt/RealTimeFaceCompare/ftp/merge/error
        String mergeErrLogDir = conf.getMergeLogDir() + "/error";
        //要写入的merge/error日志路径：/opt/RealTimeFaceCompare/ftp/merge/error/error.log
        String writeErrFile = mergeErrLogDir + "/error.log";
        //获取processLogDir目录下除去error日志所有文件绝对路径
        FileFactory fileFactory = new FileFactory(processLogDir, writingLogFile);

        List<String> processFiles = fileFactory.getAllProcessLogAbsPath();
        //获取processLogDir目录下除去最大,error和000.log的绝对路径
        List<String> backupLogAbsPath = fileFactory.getAllBackupLogAbsPath();
        System.out.println(("==========" + "recoverNotProDataTest2：测试发送未处理数据代码，发送Kafka数据" + "=========="));
        if (processFiles != null && processFiles.size() != 0) {
            for (String processFile : processFiles) {
                String receiveFile = mergeUtil.getRecFilePathFromProFile(processFile);
                System.out.println("========对应receive绝对路径为:" + receiveFile);
                if (mergeUtil.isFileExist(receiveFile)) {
                    RowsListFactory rowsListFactory = new RowsListFactory(processFile, receiveFile);
                    //获取未处理的数据
                    List<String> notProRows = rowsListFactory.getNotProRows();
                    //用于标记成功数据
                    long successRowCount = 0;
                    //用于标记失败数据
                    long failureRowCount = 0;
                    System.out.println("=====未处理的数据长度:" + notProRows.size());
                    for (int j = 0; j < notProRows.size(); j++) {
                        String row = notProRows.get(j);
                        //获取未处理数据的ftpUrl
                        LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                        String ftpUrl = event.getPath();
                        System.out.println("=====未处理的数据对应的 ftpUrl:" + ftpUrl);
                        FaceObject faceObject = GetFaceObject.getFaceObject(row);
                        if (faceObject != null) {
                            SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                            sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                            boolean success = sendDataToKafka.isSuccessToKafka();
                            if (j == 0 && !success) {
                                System.out.println("first data send to Kafka failure");
                            } else {
                                event.setTimeStamp(System.currentTimeMillis());
                                if (success) {
                                    //向对应的processFile中写入日志
                                    event.setStatus("0");
                                    System.out.println("=====Send to Kafka success,write log to processFile :" + processFile);
                                    mergeUtil.writeMergeFile(event, processFile);
                                    successRowCount++;
                                } else {
                                    //发送Kafka失败,将日志写到merge目录下的error日志文件中
                                    event.setStatus("1");
                                    System.out.println("=====Send to Kafka failure ,write log to errorLogFile :" + writeErrFile);
                                    mergeUtil.writeMergeFile(event, processFile);
                                    mergeUtil.writeMergeFile(event, writeErrFile);
                                    failureRowCount++;
                                }
                            }
                        }
                    }
                    System.out.println("=====发送Kafka成功数据有:" + successRowCount + "条=====");
                    System.out.println("=====发送Kafka失败数据有:" + failureRowCount + "条=====");
                    long rowCount = successRowCount + failureRowCount;
                    if (rowCount == notProRows.size()) {
                        System.out.println("处理process文件完成，移动process文件和receive文件到success目录下");
                        if (backupLogAbsPath.contains(processFile)) {
                            System.out.println("=====备份processFile文件:" + processFile);
                            String sucProFilePath = mergeUtil.getSuccessFilePath(processFile);
                            mergeUtil.moveFile(processFile, sucProFilePath);
                            System.out.println("=====备份receiveFile文件:" + receiveFile);
                            String sucRecFilePath = mergeUtil.getSuccessFilePath(receiveFile);
                            mergeUtil.moveFile(receiveFile, sucRecFilePath);
                        }
                    } else {
                        System.out.println("send to Kafka data less than NotProRows size, Please check it!");
                    }
                } else {
                    //对应receive 文件不存在，将process文件移动到success目录下
                    System.out.println("Can't find receiveFile,move processFile To SuccessDir" + processFile);
                    String successFilePath = mergeUtil.getSuccessFilePath(processFile);
                    mergeUtil.moveFile(processFile, successFilePath);
                }
            }
        } else {
            System.out.println("The path of " + processLogDir + "is Nothing");
        }

    }
}

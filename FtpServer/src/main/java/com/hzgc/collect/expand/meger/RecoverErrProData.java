package com.hzgc.collect.expand.meger;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 恢复处理出错的数据（马燊偲）
 */
public class RecoverErrProData {

    private Logger LOG = Logger.getLogger(RecoverErrProData.class);
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SPLIT = ",";
    private static final String mergeProPath = "/opt/logdata/merge/process/";


    public boolean RecoverErrProData(CommonConf commonConf){

        //初始化FileUtil工具类
        FileUtil fileUtil = new FileUtil();
        LogEvent logEvent = new LogEvent();

        //标记恢复数据是否成功，默认false
        boolean recoverSuccess = false;

        //获取processLog的根目录
        String processLogDir = commonConf.getProcessLogDir();
        //调用FileFactory，传入process日志根目录
        FileFactory fileFactory = new FileFactory(processLogDir);
        //获取所有process日记文件的绝对路径，放入一个List中（proFilePaths）
        List<String> proFilePaths = fileFactory.getAllProcessFiles();

        //若proFilePaths这个list不为空（process日志根目录下有日志）
        if (proFilePaths != null && !(proFilePaths.size() == 0)){
            for (int i = 0; i < proFilePaths.size(); i++) {
                String processFile = proFilePaths.get(i);
                String receiveFile = fileUtil.getRecFileFromProFile(processFile);
                //判断processFile文件对应的receiveFile是否存在
                if (!fileUtil.isFileExist(receiveFile)) {
                    //若对应的receiveFile不存在，删除这个processFile
                    fileUtil.deleteFile(processFile);
                }
                //若processFile文件对应的receiveFile存在，则获取处理出错的数据
                else {
                    //获取队列ID
                    String queueID = processFile.substring(processFile.lastIndexOf("-") + 1, processFile.lastIndexOf("/"));
                    //初始化RowsListFactory工厂类
                    RowsListFactory rowsListFactory = new RowsListFactory(processFile, receiveFile);
                    //调用getErrProRows()方法，取出processFile和receiveFile中处理出错的数据
                    List<String> errProFiles = rowsListFactory.getErrProRows();

                    //判断errProFiles是否为空，若为空，则没有出错的数据需要处理
                    if (errProFiles == null || errProFiles.size() == 0){
                        return true;
                    } else {
                        for (int j = 0; j < errProFiles.size(); j++) {

                            String row = errProFiles.get(j);
                            //根据processFile的日志路径，获取对应mergeReceiveFile的日志路径
                            String mergeRecFilePath = fileUtil.getMergeRecFilePath(receiveFile);
                            //将errProFiles中每一条数据，保存到merge 的receive目录下对应的文件名中
                            fileUtil.writeMergeFile(row, mergeRecFilePath);

                            //按分隔符切分每一条记录，记录格式为：
                            //"count":0, "url":"ftp://s100:/2018/01/09", "timestamp":"2018-01-02", "status":"0"
                            String splits[] = errProFiles.get(j).split(SPLIT);
                            //获取处理出错的数据的ftpUrl：例如ftp://s100:/2018/01/09
                            String ftpUrl = splits[1].substring(splits[1].indexOf(":") + 2, splits[1].lastIndexOf("\""));
                            //获取该条数据的序列号
                            String rowNumber = splits[0].substring(splits[0].indexOf(":") + 1);
                            long count = Long.valueOf(rowNumber);

                            //根据路径取得对应的图片，并提取特征，封装成FaceObject，发送Kafka
                            FaceObject faceObject = GetFaceObject.getFaceObject(row);
                            if (faceObject != null) {
                                SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                                sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                                boolean success = sendDataToKafka.isSuccessToKafka();

                                //并向对应的merge/process/processFile中写入日志记录（发送kafka是否成功）
                                LOG.info("Write log queueID is" + queueID + "" + processFile);
                                //根据processFile的日志路径，获取对应mergeProcessFile的日志路径
                                String mergeProFilePath = fileUtil.getMergeProFilePath(processFile);
                                //若未发送成功，不修改这行数据记录的状态（原本记录的状态为发送失败），写入merge/process的日志记录
                                if (!success) {
                                    //将errProFiles中每一条数据，写入到该日志
                                    fileUtil.writeMergeFile(row, mergeProFilePath);
                                } else {
                                    //若发送成功，修改这行数据记录的状态为发送成功，写入merge/process的日志记录
                                    String rowAftSuc = row.substring(row.split(SPLIT)[3].indexOf(":\"")) + "1\"";
                                    fileUtil.writeMergeFile(rowAftSuc, mergeProFilePath);
                                }
                            }
                        }
                    }
                    //删除原来的processFile和receiveFile
                    fileUtil.deleteFile(processFile, receiveFile);
                }
            }

            //扫描merge/process的根目录，得到处理过的日记文件的绝对路径totalFilePaths
            FileFactory mergeFileFactory = new FileFactory(mergeProPath);
            List<String> mergeProFilePaths = fileFactory.getAllProcessFiles();
            //若mergeProFilePaths这个list不为空（merge/process日志根目录下有日志）
            if (mergeProFilePaths != null && !(mergeProFilePaths.size() == 0)){
                for (int i = 0; i < mergeProFilePaths.size(); i++) {
                    String mergeProcessFile = mergeProFilePaths.get(i);
                    String mergeReceiveFile = fileUtil.getRecFileFromProFile(mergeProcessFile);
                    //判断mergeProcessFile文件对应的mergeReceiveFile是否存在
                    if (!fileUtil.isFileExist(mergeReceiveFile)) {
                        //若对应的mergeReceiveFile不存在，删除这个mergeProcessFile
                        fileUtil.deleteFile(mergeProcessFile);
                    }
                    //若mergeProcessFile文件对应的mergeReceiveFile存在，则获取处理出错的数据
                    else {
                        //初始化RowsListFactory工厂类
                        RowsListFactory rowsListFactoryV2 = new RowsListFactory(mergeProcessFile, mergeReceiveFile);
                        //调用getErrProRows()方法，取出mergeProcessFile和mergeReceiveFile中处理出错的数据
                        List<String> mergeErrProFiles = rowsListFactoryV2.getErrProRows();
                        //删除原来的mergeProcessFile和mergeReceiveFile
                        fileUtil.deleteFile(mergeProcessFile, mergeReceiveFile);

                        //用mergeErrProFiles中的数据，覆盖原mergeReceiveFile日志中的内容
                        // 例如/opt/logdata/merge/receive/r-0/000000000001.log
                        for (int j = 0; j < mergeErrProFiles.size() ; j++) {
                            String row = mergeErrProFiles.get(j);
                            fileUtil.writeMergeFile(row, mergeReceiveFile);
                        }
                        //若mergeErrProFiles中无内容，跳过；若有内容：
                        // 遍历mergeErrProFiles，将内容发送到kafka，并覆盖日志/opt/logdata/merge/receive/r-0/000000000001.log
                        if (mergeErrProFiles != null && mergeErrProFiles.size() == 0){
                            for (int k = 0; k < mergeErrProFiles.size() ; k++) {
                                String row = mergeErrProFiles.get(k);

                                //按分隔符切分每一条记录，记录格式为：
                                //"count":0, "url":"ftp://s100:/2018/01/09", "timestamp":"2018-01-02", "status":"0"
                                String splits[] = mergeErrProFiles.get(k).split(SPLIT);
                                //获取处理出错的数据的ftpUrl：例如ftp://s100:/2018/01/09
                                String ftpUrl = splits[1].substring(splits[1].indexOf(":") + 2, splits[1].lastIndexOf("\""));
                                //根据路径取得对应的图片，并提取特征，封装成FaceObject，发送Kafka
                                FaceObject faceObject = GetFaceObject.getFaceObject(row);
                                if (faceObject != null) {
                                    SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                                    sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                                    boolean success = sendDataToKafka.isSuccessToKafka();

                                    //并覆盖对应的merge/process/processFile日志记录（发送kafka是否成功）
                                    //若未发送成功，不修改这行数据记录的状态（原本记录的状态为发送失败），覆盖merge/process的日志记录
                                    if (!success) {
                                        fileUtil.writeMergeFile(row, mergeProcessFile);
                                    } else {
                                        //若发送成功，修改这行数据记录的状态为发送成功，覆盖merge/process的日志记录
                                        String rowAftSuc = row.substring(row.split(SPLIT)[3].indexOf(":\"")) + "1\"";
                                        fileUtil.writeMergeFile(rowAftSuc, mergeProcessFile);
                                    }
                                }

                                //对比/opt/logdata/merge/receive/r-0/000000000001.log  和 /opt/logdata/merge/receive/r-0/000000000001.log
                                //若为空，则删除这两个文件。若不为空，不进行处理，退出程序。
                                RowsListFactory rowsListFactoryV3 = new RowsListFactory(mergeProcessFile, mergeReceiveFile);
                                List<String> mergeErrProFilesV2 = rowsListFactoryV3.getErrProRows();
                                if (mergeErrProFilesV2 == null || mergeErrProFilesV2.size() == 0){
                                    fileUtil.deleteFile(mergeProcessFile, mergeReceiveFile);
                                    recoverSuccess = true;
                                }
                                else { //do nothing

                                }
                            }
                        } else { //do nothing

                        }
                    }
                }
            }
        }
        //若proFilePaths这个list为空（process日志根目录下无日志），结束
        else {

        }

        return recoverSuccess;
    }
}

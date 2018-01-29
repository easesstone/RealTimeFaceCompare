package com.hzgc.collect.expand.meger;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 恢复处理出错的数据，作为Ftp的一个线程。（马燊偲）
 *
 * 整体流程：
 * 1，遍历process 目录中的文件，得到日记文件的一个绝对路径，放入一个List中，proFilePaths
 * 2，判断proFilePaths是否为空（process 目录下是否有文件），若为空，结束；
 *	    若不为空，遍历proFilePaths的每一个文件，例如如下：
 *          1，/opt/logdata/process/p-0/000000000001.log
 *          然后查看对应的 /opt/logdata/receive/r-0/000000000001.log 文件是否存在
 *              （1）存在：
 *                      1，读取两个文件的内容到一个List 里面，
 *                      2，排序，对比，取得处理出错的数据列表 errProFiles
 *                      3，遍历errProFiles，每一条出错数据存储到merge的receive目录下对应的文件名中，
 *                          例如/opt/logdata/merge/receive/r-0/000000000001.log。
 *                          并删除 /opt/logdata/process/p-0/000000000001.log  和
 *                          /opt/logdata/receive/r-0/000000000001.log
 *                      4，遍历errProFiles，提取特征发送Kafka，
 *                          同时把处理日记发送到/opt/logdata/merge/process/p-0/000000000001.log
 *              （2）不存在：
 *                      把/opt/logdata/process/p-0/000000000001.log 文件删除，跳过这层循环。
 *          2，遍历/opt/logdata/merge/process/下的所有文件，放到mergeProFilePaths中------->List
 *          3，遍历mergeProFilePaths
 *              1，得到比如/opt/logdata/merge/process/p-0/000000000001.log，
 *              和文件/opt/logdata/merge/receive/r-0/000000000001.log 进行对比
 *              2，把/opt/logdata/merge/receive/r-0/000000000001.log
 *              和 /opt/logdata/merge/receive/r-0/000000000001.log 放到一个totalList  里
 *              3，对totalList 进行排序，比较，得到diffListV1，
 *              然后覆盖/opt/logdata/merge/receive/r-0/000000000001.log 的内容，
 *                      1，diffListV1 没有内容，则跳过，有内容，进行下面的2
 *                      2，遍历diffListV1,把处理的内容发送到Kafka ，
 *                      同时把日记覆盖掉/opt/logdata/merge/process/r-0/000000000001.log
 *          4，对比/opt/logdata/merge/receive/r-0/000000000001.log  和
 *              /opt/logdata/merge/receive/r-0/000000000001.log，获取两个文件出错的数据集合列表。
 *              1，若列表为空，则删除这两个文件。
 *              2，若不为空，不进行处理，退出程序。
 * 3，结束
 *
 */
public class RecoverErrProData implements Runnable{

    private Logger LOG = Logger.getLogger(RecoverErrProData.class);
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final String MERGE_PRO_PATH = "/opt/logdata/merge/process/";
    private static CommonConf commonConf;

    //构造函数
    RecoverErrProData(CommonConf commonConf){
        RecoverErrProData.commonConf = commonConf;
    }

    @Override
    public void run() {
        //初始化FileUtil工具类
        FileUtil fileUtil = new FileUtil();
        LogEvent logEvent = new LogEvent();

        //获取processLog的根目录
        String processLogDir = commonConf.getProcessLogDir();
        //调用FileFactory，传入process日志根目录
        FileFactory fileFactory = new FileFactory(processLogDir);
        //获取所有process日记文件的绝对路径，放入一个List中（proFilePaths）
        List<String> proFilePaths = fileFactory.getAllProcessFiles();

        //若proFilePaths这个list不为空（process日志根目录下有日志）
        if (proFilePaths != null && proFilePaths.size() != 0){ // V1 if start
            for (String processFile : proFilePaths) { // V1-A foreach start
                String receiveFile = fileUtil.getRecFileFromProFile(processFile);
                //判断processFile文件对应的receiveFile是否存在
                if (!fileUtil.isFileExist(receiveFile)) { // V1-B if start
                    //若对应的receiveFile不存在，删除这个processFile
                    fileUtil.deleteFile(processFile);
                } // V1-B if end
                //若processFile文件对应的receiveFile存在，则获取处理出错的数据
                else { // V1-B else start
                    //获取队列ID
                    String queueID = processFile.substring(processFile.lastIndexOf("-") + 1, processFile.lastIndexOf("/"));
                    //初始化RowsListFactory工厂类
                    RowsListFactory rowsListFactory = new RowsListFactory(processFile, receiveFile);
                    //调用getErrProRows()方法，取出processFile和receiveFile中处理出错的数据
                    List<String> errProFiles = rowsListFactory.getErrProRows();

                    //判断errProFiles是否为空，若不为空，则需要处理出错数据
                    if (errProFiles != null && errProFiles.size() != 0) { // V1-C if start
                        for (String row : errProFiles) { // V1-D foreach start
                            //用JSONHelper将某行数据转化为LogEvent格式
                            LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                            //根据processFile的日志路径，获取对应mergeReceiveFile的日志路径
                            String mergeRecFilePath = fileUtil.getMergeRecFilePath(receiveFile);
                            //将errProFiles中每一条数据，保存到merge 的receive目录下对应的文件名中
                            fileUtil.writeMergeFile(event, mergeRecFilePath);

                            //每一条记录的格式为：
                            //"count":0, "url":"ftp://s100:/2018/01/09", "timestamp":"2018-01-02", "status":"0"
                            //用LogEvent获取该条数据的ftpUrl
                            String ftpUrl = event.getPath();
                            //获取该条数据的序列号
                            long count = event.getCount();

                            //根据路径取得对应的图片，并提取特征，封装成FaceObject，发送Kafka
                            FaceObject faceObject = GetFaceObject.getFaceObject(row);
                            if (faceObject != null) { // V1-E if start
                                SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                                sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                                boolean success = sendDataToKafka.isSuccessToKafka();

                                //并向对应的merge/process/processFile中写入日志记录（发送kafka是否成功）
                                LOG.info("Write log queueID is" + queueID + "" + processFile);
                                //根据processFile的日志路径，获取对应mergeProcessFile的日志路径
                                String mergeProFilePath = fileUtil.getMergeProFilePath(processFile);

                                //向对应的merge/process/processFile写入日志
                                logEvent.setPath(ftpUrl);
                                if (success){
                                    logEvent.setStatus("0");
                                }
                                else {
                                    logEvent.setStatus("1");
                                }
                                logEvent.setCount(count);
                                logEvent.setTimeStamp(Long.valueOf(SDF.format(new Date())));
                                fileUtil.writeMergeFile(event, mergeProFilePath);
                            } // V1-E if end
                        } // V1-D for each end
                    } // V1-C if end
                    //删除原来的processFile和receiveFile
                    fileUtil.deleteFile(processFile, receiveFile);
                } // V1-B else end
            } // V1-A foreach end

            //扫描merge/process的根目录，得到处理过的日记文件的绝对路径totalFilePaths
            FileFactory mergeFileFactory = new FileFactory(MERGE_PRO_PATH);
            List<String> mergeProFilePaths = mergeFileFactory.getAllProcessFiles();
            //若mergeProFilePaths这个list不为空（merge/process日志根目录下有日志）
            if (mergeProFilePaths != null && mergeProFilePaths.size() != 0){ // V2 if start
                for (String mergeProcessFile : mergeProFilePaths) { // V2-A foreach start
                    String mergeReceiveFile = fileUtil.getRecFileFromProFile(mergeProcessFile);
                    //判断mergeProcessFile文件对应的mergeReceiveFile是否存在
                    if (!fileUtil.isFileExist(mergeReceiveFile)) { // V2-B if start
                        //若对应的mergeReceiveFile不存在，删除这个mergeProcessFile
                        fileUtil.deleteFile(mergeProcessFile);
                    } // V2-B if end
                    //若mergeProcessFile文件对应的mergeReceiveFile存在，则获取处理出错的数据
                    else { // V2-B else start
                        //获取队列ID
                        String queueID = mergeProcessFile.substring(mergeProcessFile.lastIndexOf("-") + 1,
                                mergeProcessFile.lastIndexOf("/"));
                        //初始化RowsListFactory工厂类
                        RowsListFactory rowsListFactoryV2 = new RowsListFactory(mergeProcessFile, mergeReceiveFile);
                        //调用getErrProRows()方法，取出mergeProcessFile和mergeReceiveFile中处理出错的数据
                        List<String> mergeErrProFiles = rowsListFactoryV2.getErrProRows();
                        //删除原来的mergeProcessFile和mergeReceiveFile
                        fileUtil.deleteFile(mergeProcessFile, mergeReceiveFile);

                        //用mergeErrProFiles中的数据，覆盖原mergeReceiveFile日志中的内容
                        // 例如/opt/logdata/merge/receive/r-0/000000000001.log
                        for (String row : mergeErrProFiles) { // V2-C foreach start
                            //用JSONHelper将某行数据转化为LogEvent格式
                            LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                            fileUtil.writeMergeFile(event, mergeReceiveFile);
                        } // V2-C foreach end
                        //若mergeErrProFiles中无内容，跳过；若有内容：
                        // 遍历mergeErrProFiles，将内容发送到kafka，并覆盖日志/opt/logdata/merge/receive/r-0/000000000001.log
                        if (mergeErrProFiles.size() != 0) { // V2-D if start
                            for (String row : mergeErrProFiles) { // V2-E foreach start
                                LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                                //用LogEvent获取该条数据的ftpUrl
                                String ftpUrl = event.getPath();
                                //获取该条数据的序列号
                                long count = event.getCount();

                                //根据路径取得对应的图片，并提取特征，封装成FaceObject，发送Kafka
                                FaceObject faceObject = GetFaceObject.getFaceObject(row);
                                if (faceObject != null) { // V2-F if start
                                    SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                                    sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);
                                    boolean success = sendDataToKafka.isSuccessToKafka();

                                    //并覆盖对应的merge/process/processFile日志记录（发送kafka是否成功）
                                    LOG.info("Rewrite log queueID is" + queueID + "" + mergeProcessFile);
                                    logEvent.setPath(ftpUrl);
                                    if (success){
                                        logEvent.setStatus("0");
                                    }
                                    else {
                                        logEvent.setStatus("1");
                                    }
                                    logEvent.setCount(count);
                                    logEvent.setTimeStamp(Long.valueOf(SDF.format(new Date())));
                                    fileUtil.writeMergeFile(event, mergeProcessFile);
                                } // V2-F if end

                                //对比两个文件：/opt/logdata/merge/receive/r-0/000000000001.log
                                //                 和 /opt/logdata/merge/process/p-0/000000000001.log
                                //获取两个文件出错的数据集合列表。若列表为空，则删除这两个文件。若不为空，不进行处理，退出程序。
                                RowsListFactory rowsListFactoryV3 = new RowsListFactory(mergeProcessFile, mergeReceiveFile);
                                List<String> mergeErrProFilesV2 = rowsListFactoryV3.getErrProRows();
                                if (mergeErrProFilesV2 == null || mergeErrProFilesV2.size() == 0) {
                                    fileUtil.deleteFile(mergeProcessFile, mergeReceiveFile);
                                } else { //do nothing

                                }
                            } // V2-E foreach end
                        } // V2-D if end
                        else { // V2-D else
                            //do nothing
                        }
                    } // V2-B else end
                } // V2-A foreach end
            } // V2 if end
        } // V1 if end

        //若proFilePaths这个list为空（process日志根目录下无日志），结束
        else { // V1 else
            //do nothing
        }

    }
}

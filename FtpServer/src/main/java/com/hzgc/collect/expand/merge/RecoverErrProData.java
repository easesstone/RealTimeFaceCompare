package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.KafkaProducer;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * 恢复处理出错的数据，作为Ftp的一个线程。（马燊偲）
 * 整体流程：
 * 1，遍历所有error日志：data/process/p-0/error/error.log
 *                                  data/process/p-1/error/error.log
 *                                  data/process/p-2/error/error.log
 *      对于每个错误日志：获取错误日志的状态：
 *              A、若文件处于lock状态，结束；
 *              B、若文件为unlock状态：
 *                  立即移动data/process/p-0/error/error.log到
 *                  /success/process/201802/p-0/error/error 2018-02-01-1522148-1758.log（用于备份）和
 *                  /merge/error/error 2018-02-01-1522148-1963.log（用于恢复处理）
 * 2，对备份目录/merge/error/ 下的所有错误文件进行扫描，
 *      将所有错误日志路径放入到一个List 里面，errLogPaths。遍历errLogPaths：
 *      1，对于errLogPaths中每一个errorN.log，遍历其中每一条数据：
 *              1，对每条数据，提取特征发送Kafka，
 *              2，同时把发送失败的数据重新写到/merge/error/下的一个新的errorN-NEW日志中，
 *              3，发送成功的数据，不进行记录。
 *      2，删除原有已处理过的错误日志errorN.log。
 * 3，结束
 */
public class RecoverErrProData implements Runnable {

    private Logger LOG = Logger.getLogger(RecoverErrProData.class);
    private static final String SUFFIX = ".log";
    private static CommonConf commonConf;

    //构造函数
    RecoverErrProData(CommonConf commonConf) {
        RecoverErrProData.commonConf = commonConf;
    }

    @Override
    public void run() {
        //初始化FileUtil工具类
        MergeUtil mergeUtil = new MergeUtil();

        //获取processLog的根目录：/opt/RealTimeFaceCompare/ftp/data/process
        String processLogDir = commonConf.getProcessLogDir();
        //获取merge/error目录：/opt/RealTimeFaceCompare/ftp/merge/error
        String mergeErrLogDir = commonConf.getMergeLogDir() + File.separator + "error";
        String ftpdataDir = commonConf.getFtpdataDir();

        //列出process目录下所有error日志路径
        List<String> allErrorDir = mergeUtil.listAllErrorLogAbsPath(processLogDir);
        for (String errFile:allErrorDir) {
            //获取每个error.log需要移动到的success和merge目录下的路径
            String successErrFile = mergeUtil.getSuccessFilePath(errFile);
            String mergeErrFile = mergeUtil.getMergeFilePath(errFile);
            //移动到merge后，拷贝一份到success
            mergeUtil.lockAndMove(errFile, mergeErrFile); //其中包括判断锁是否存在
            mergeUtil.copyFile(mergeErrFile, successErrFile);
        }

        //获取merge/error下所有error日记文件的绝对路径，放入一个List中（errLogPaths）
        List<String> errFilePaths = mergeUtil.listAllFileAbsPath(mergeErrLogDir);
        //若errLogPaths这个list不为空（merge/error下有错误日志）
        if (errFilePaths != null && errFilePaths.size() != 0) { // V-1 if start
            //用于标记kafka正在处理第几条数据
            int flag = 0;
            //对于每一个error.log
            for (String errorFilePath : errFilePaths) {
                //获取其中每一行数据
                List<String> errorRows = mergeUtil.getAllContentFromFile(errorFilePath);
                //判断errorRows是否为空，若不为空，则需要处理出错数据
                if (errorRows != null && errorRows.size() != 0) { // V-2 if start
                    for (String row : errorRows) {
                        //用JSONHelper将某行数据转化为LogEvent格式
                        LogEvent event = JSONHelper.toObject(row, LogEvent.class);

                        //每一条记录的格式为：
                        //"count":0, "url":"ftp://s100:/2018/01/09", "timestamp":"2018-01-02", "status":"0"
                        //用LogEvent获取该条数据的ftpUrl
                        String ftpUrl = event.getPath();
                        //根据路径取得对应的图片，并提取特征，封装成FaceObject，发送Kafka
                        FaceObject faceObject = GetFaceObject.getFaceObject(row,ftpdataDir);
                        if (faceObject != null) { // V-3 if start
                            SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
                            sendDataToKafka.sendKafkaMessage(KafkaProducer.getFEATURE(), ftpUrl, faceObject);

                            if ( flag == 0) {
                                //确认kafka接收到第一条数据后，再获取success值。否则获取到success值过快，会获取到false。
                                //只在处理第一条数据时，执行此步骤
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            boolean success = sendDataToKafka.isSuccessToKafka();

                            //若发送kafka不成功，将错误日志写入/merge/error/下一个新的errorN-NEW日志中
                            String mergeErrFileNew = errorFilePath.replace(SUFFIX,"")+"-N"+SUFFIX;
                            if (!success) {
                                mergeUtil.writeMergeFile(event, mergeErrFileNew);
                            }
                        } // V-3 if end：faceObject不为空的判断结束
                        flag ++;
                    }
                } // V-2 if end：errorRows为空的判断结束
                //删除已处理过的error日志
                mergeUtil.deleteFile(errorFilePath);
            }
        } else { //若merge/error目录下无日志
            LOG.info("Nothing in " + mergeErrLogDir);
        } // V-1 if end：对merge/error下是否有日志的判断结束
    }
}
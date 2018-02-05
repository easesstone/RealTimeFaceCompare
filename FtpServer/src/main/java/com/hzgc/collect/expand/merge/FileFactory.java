package com.hzgc.collect.expand.merge;

import java.util.List;


/**
 * 数据封装类：FileFactory（马燊偲）
 * 成员变量含义：
 * processParentDir：process日志的根目录
 * processFile：process日志根目录下所有文件
 * receiveFilePath：根据某一个process日志路径，获得的对应receive日志路径
 */
class FileFactory {

    private  FileUtil fileUtil = new FileUtil();

    private  String processParentDir;
    private String writingLogFile;
    private  List<String> allProcessFiles;
    private  List<String> allFiles;

    //有参构造函数，传入需处理的日志根路径，
    // 以及CommonConf中的logName（000000.log）
    FileFactory(String processLogDir, String writingLogFile) {
        this.processParentDir = processLogDir;
        this.writingLogFile = writingLogFile;
        setAllProcessFiles();
    }

    /**
     * set方法
     * 列出process日志根目录下所有文件
     */

    private void setAllProcessFiles() {
        allProcessFiles = fileUtil.listAllProcessFileDir(processParentDir, writingLogFile);
    }


    /**
     * get方法
     */
    List<String> getAllProcessFiles() {
        return allProcessFiles;
    }

}

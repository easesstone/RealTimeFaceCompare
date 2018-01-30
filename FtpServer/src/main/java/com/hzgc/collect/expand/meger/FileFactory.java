package com.hzgc.collect.expand.meger;

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
    private  List<String> allProcessFiles;

    //有参构造函数，传入需处理的日志根路径
    FileFactory(String processLogDir) {
        this.processParentDir = processLogDir;
        setAllProcessFiles();
    }

    /**
     * set方法
     * 列出process日志根目录下所有文件
     */

    private void setAllProcessFiles() {
        allProcessFiles = fileUtil.listAllFileOfDir(processParentDir);
    }

    /**
     * get方法
     */
    List<String> getAllProcessFiles() {
        return allProcessFiles;
    }

}

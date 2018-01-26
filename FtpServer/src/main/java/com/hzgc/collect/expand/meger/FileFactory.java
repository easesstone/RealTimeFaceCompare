package com.hzgc.collect.expand.meger;

import java.util.List;


/**
 * 数据封装类：FileFactory
 * 成员变量含义：
 * processParentDir：process日志的根目录
 * processFile：process日志根目录下所有文件
 * receiveFilePath：根据某一个process日志路径，获得的对应receive日志路径
 */
public class FileFactory {

    private static FileUtil fileUtil = new FileUtil();

    private static String processParentDir;
    private static List<String> allProcessFiles;
    private static String receiveFilePath;


    //有参构造函数，传入需处理的日志根路径
    public FileFactory(String processLogDir) {
        this.processParentDir = processLogDir;
        setAllProcessFiles();

//        //列出process日志根目录下所有文件
//        this.allProcessFiles = fileUtil.listAllFileOfDir(processParentDir);
//        //根据某一个process日志路径，获得对应receive日志路径
//        this.receiveFilePath = fileUtil.getRecFileFromProFile(this.processParentDir);
    }

    /**
     * set方法
     */

    //列出process日志根目录下所有文件
    private static List<String> setAllProcessFiles() {
        return allProcessFiles = fileUtil.listAllFileOfDir(processParentDir);
    }

    //根据某一个process日志路径，获得对应receive日志路径
//    private static String setReceiveFilePath(String processFilePath) {
//        return receiveFilePath = fileUtil.getRecFileFromProFile(processFilePath);
//    }

    /**
     * get方法
     */
    public List<String> getAllProcessFiles() {
        //System.out.println(allProcessFiles);
        return allProcessFiles;
    }

//    public String getReceiveFilePath() {
//        return receiveFilePath;
//    }
}

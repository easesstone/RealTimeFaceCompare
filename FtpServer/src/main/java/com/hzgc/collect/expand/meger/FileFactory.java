package com.hzgc.collect.expand.meger;

import java.util.List;


/**
 * 数据封装类：FileFactory
 * 成员变量含义：
 * processParentDir：process日志的根目录
 * processFile：process日志根目录下所有文件
 */
public class FileFactory {

    private FileUtil fileUtil = new FileUtil();

    private String processParentDir; //要处理的日志路径根目录
    private List<String> processFiles; //要处理的所有日志名列表

    //无参构造函数
    public FileFactory(){
    }

    //有参构造函数，传入需处理的日志根路径
    public String FileFactory(String processLogDir) {
        return processParentDir = processLogDir;
    }

    /**
     * set方法
     * 列出process日志根目录下所有文件
     */
    private List<String> setProcessFiles() {
        return processFiles = fileUtil.listAllFileOfDir(processParentDir);
    }

    /**
     * get方法
     */
    public List<String> getProcessFiles() {
        return processFiles;
    }
}

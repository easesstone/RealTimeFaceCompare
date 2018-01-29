package com.hzgc.collect.expand.meger;

import java.util.List;

/**
 * 数据封装类：RowsListFactory
 * 成员变量含义：
 * allDiffRows：合并后日志中所有不同的数据；
 * notProRows：未处理的数据
 * errProRows：处理失败的数据
 */
public class RowsListFactory {

    private  String receiveFileDir; //要处理的一个process日志对应的receive日志路径

    //初始化要用到的两个工具类
    private  FileUtil fileUtil = new FileUtil();
    private  FindDiffRows findDiffRows = new FindDiffRows();

    private  List<String> allDiffRows;
    private  List<String> notProRows;
    private  List<String> errProRows;

    //有参构造函数，传入需要处理的某个process文件路径
    public RowsListFactory(String processFileDir){
        //调用FileUtil中的方法，获取process文件对应的receive文件
        this.receiveFileDir = fileUtil.getRecFileFromProFile(processFileDir);
        this.setAllDiffRows(processFileDir);
        this.setNotProRows();
        this.setErrProRows();
    }

    /**
     * set 方法
     */
    private  List<String> setAllDiffRows(String processFileDir) {
        List<String> allContentRows = fileUtil.getAllContentFromFile(processFileDir);
        return allDiffRows = findDiffRows.getAllDiffRows(allContentRows);
    }

    private  List<String> setNotProRows() {
        return notProRows = findDiffRows.getNotProRows(allDiffRows);
    }

    private  List<String> setErrProRows() {
        return errProRows = findDiffRows.getErrProRows(allDiffRows);
    }

    /**
     * get 方法
     */
    public List<String> getAllDiffRows() {
        return allDiffRows;
    }

    public List<String> getNotProRows() {
        return notProRows;
    }

    public List<String> getErrProRows() {
        return errProRows;
    }
}

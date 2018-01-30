package com.hzgc.collect.expand.merge;

import java.util.List;

/**
 * 数据封装类：RowsListFactory
 * 用于获取一个processFile与其对应的receiveFile之间不同的数据。
 * 成员变量含义：
 * allDiffRows：合并后日志中所有不同的数据；
 * notProRows：未处理的数据（receiveFile中有，processFile中没有的数据）
 * errProRows：处理失败的数据（receiveFile中状态为0，processFile中状态为1的数据）
 */
public class RowsListFactory {

    //初始化要用到的两个工具类
    private  FileUtil fileUtil = new FileUtil();
    private  FindDiffRows findDiffRows = new FindDiffRows();

    private  List<String> allDiffRows;
    private  List<String> notProRows;
    private  List<String> errProRows;

    //有参构造函数，传入需要处理的某个process文件路径，及其对应的receiveFileDir文件路径
    public RowsListFactory(String processFileDir, String receiveFileDir){
        setAllDiffRows(processFileDir, receiveFileDir);
        setNotProRows();
        setErrProRows();
    }

    /**
     * set 方法
     */
    private void setAllDiffRows(String processFileDir, String receiveFileDir) {
        List<String> allContentRows = fileUtil.getAllContentFromFile(processFileDir, receiveFileDir);
        allDiffRows = findDiffRows.getAllDiffRows(allContentRows);
    }

    private void setNotProRows() {
        notProRows = findDiffRows.getNotProRows(allDiffRows);
    }

    private void setErrProRows() {
        errProRows = findDiffRows.getErrProRows(allDiffRows);
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

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

    FileFactory fileFactory = new FileFactory();
    FindDiffRows findDiffRows = new FindDiffRows();

    private List<String> allDiffRows;
    private List<String> notProRows;
    private List<String>  errProRows;

    public RowsListFactory(){
    }

    /**
     * set 方法
     */
    private List<String> setAllDiffRows() {
        return allDiffRows = findDiffRows.getAllDiffRows(fileFactory.getProcessFiles());
    }

    private List<String> setNotProRows() {
        return notProRows = findDiffRows.getNotProRows(fileFactory.getProcessFiles());
    }

    private List<String> setErrProRows() {
        return errProRows = findDiffRows.getErrProRows(fileFactory.getProcessFiles());
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

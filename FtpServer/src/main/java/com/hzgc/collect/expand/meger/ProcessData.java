package com.hzgc.collect.expand.meger;

import java.util.List;

/**
 *数据封装类：ProcessData
 * 成员变量含义：
 * processParentDir：process的根目录
 * processFile：process目录下所有文件
 * allDiffRows：合并后日志中所有不同的数据；
 * needProRows：待处理数据（未处理或处理失败的数据）
 */
public class ProcessData {
    private String processParentDir;
    private List<String> processFile;
    private List<String> allDiffRows;
    private List<String> needProRows;

    public ProcessData(String processParentDir) {
        this.processParentDir = processParentDir;
    }

    public String getProcessParentDir() {
        return processParentDir;
    }

    public void setProcessParentDir(String processParentDir) {
        this.processParentDir = processParentDir;
    }

    public List<String> getProcessFile() {
        return processFile;
    }

    public void setProcessFile(List<String> processFile) {
        this.processFile = processFile;
    }

    public List<String> getAllDiffRows() {
        return allDiffRows;
    }

    public void setAllDiffRows(List<String> allDiffRows) {
        this.allDiffRows = allDiffRows;
    }

    public List<String> getNeedProRows() {
        return needProRows;
    }

    public void setNeedProRows(List<String> needProRows) {
        this.needProRows = needProRows;
    }

    @Override
    public String toString() {
        return "ProcessData{" +
                "processParentDir='" + processParentDir + '\'' +
                ", processFile=" + processFile +
                ", allDiffRows=" + allDiffRows +
                ", needProRows=" + needProRows +
                '}';
    }
}

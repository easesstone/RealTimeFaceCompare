package com.hzgc.collect.expand.meger;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 工具类FindDiffRows中包含以下三个方法：
 * <p>
 * getNotProRows：获取集合中未处理的所有行；
 * getErrProRows：获取集合中处理失败的所有行；
 * getAllDiffRows：获取集合中不同行；
 */
public class FindDiffRows {
    private Logger LOG = Logger.getLogger(FindDiffRows.class);

    /**
     * 获取日志中未处理的的所有数据
     *
     * @param allRows 日志合并后的所有行
     * @return List对象  未处理数据的集合
     */
    public List<String> getNotProRows(List<String> allRows) {
        List<String> notProList = new ArrayList<>();
        String row;
        if (allRows == null || allRows.size() == 0) {
            LOG.warn("The unionAllRows size is None");
            return notProList;
        } else if (allRows.size() == 1) {
            LOG.info("The unionAllRows size is OnlyOne");
            String processState = getProcessState(allRows.get(0));
            if (processState.equals("0")) {
                notProList.add(allRows.get(0));
            }
            return notProList;
        } else {
            Collections.sort(allRows);
            for (int i = 1; i < allRows.size() - 2; i++) {
                row = allRows.get(i);
                String lineNumber = getRowNumber(row);
                //将日志中一行数据的序号与其上下行的序号进行比较，
                // 存在相同则表示已处理，没有相同表示未处理
                if (!lineNumber.equals(getRowNumber(allRows.get(i - 1)))
                        && !lineNumber.equals(getRowNumber(allRows.get(i + 1)))) {
                    notProList.add(row);
                }
            }
            //判断第一行数据是否已经处理
            if (!getRowNumber(allRows.get(0)).equals(getRowNumber(allRows.get(1)))) {
                notProList.add(allRows.get(0));
            }
            //判断最后一行数据是否已经处理
            if (!getRowNumber(allRows.get(allRows.size() - 1))
                    .equals(getRowNumber(allRows.get(allRows.size() - 2)))) {
                notProList.add(allRows.get(allRows.size() - 1));
            }
            return notProList;
        }

    }

    /**
     * 获取合并后日志中数据处理失败的集合
     *
     * @param allRows 日志合并后的所有行
     * @return List对象  合并后集合中数据处理失败的集合
     */
    public List<String> getErrProRows(List<String> allRows) {
        List<String> failList = new ArrayList<>();
        String tmp;
        if (allRows == null || allRows.size() == 0) {
            LOG.warn("The unionAllRows size is None");
            return failList;
        } else {
            Collections.sort(allRows);
            for (String allRow : allRows) {
                tmp = allRow;
                String processState = getProcessState(tmp);
                //根据状态是否为零判断数据是否处理成功
                if (!processState.equals("0")) {
                    failList.add(tmp);
                }
            }
            return failList;
        }
    }


    /**
     * 获取集合中不同行
     *
     * @param allRows 合并后日志集合
     * @return List对象       返回合并后不同行的集合
     */
    public List<String> getAllDiffRows(List<String> allRows) {
        List<String> rows = new ArrayList<>();
        String row;
        if (allRows == null || allRows.size() == 0) {
            LOG.warn("The unionAllRows size is None");
        } else if (allRows.size() == 1) {
            LOG.info("The unionAllRows size is OnlyOne");
            rows.add(allRows.get(0));
        } else {
            Collections.sort(allRows);
            for (int i = 1; i < allRows.size() - 2; i++) {
                row = allRows.get(i);
                if (!row.equals(allRows.get(i - 1)) && !row.equals(allRows.get(i + 1))) {
                    rows.add(row);
                }
            }
            if (!allRows.get(0).equals(allRows.get(1))) {
                rows.add(allRows.get(0));
            }
            if (!allRows.get(allRows.size() - 1).equals(allRows.get(allRows.size() - 2))) {
                rows.add(allRows.get(allRows.size() - 1));
            }

        }
        return rows;
    }

    /**
     * 获取一行日志中数据在日志中的序号
     *
     * @param row 日志中一行数据
     * @return String        数据在日志中序号
     */
    private String getRowNumber(String row) {
        String rowNumber = "";
        String split = ",";
        if (row == null || row.length() == 0) {
            LOG.warn("This row of data is empty");
        } else {
            rowNumber = row.substring(0, row.indexOf(split));
        }
        return rowNumber;
    }

    /**
     * 获取一行日志中数据处理状态
     *
     * @param row 日志中一行数据
     * @return String        数据处理的状态
     */
    private String getProcessState(String row) {
        String processState = "";
        String split = ",";
        if (row == null || row.length() == 0) {
            LOG.warn("This row of data is empty");
        } else {
            processState = row.substring(row.lastIndexOf(split) + 1);
        }
        return processState;
    }
}

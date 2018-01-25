package com.hzgc.collect.expand.meger;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindDiffRows {
    private static Logger LOG = Logger.getLogger(FindDiffRows.class);

    /**
     * 获取日志中未处理的的所有数据
     *
     * @param allRows 日志合并后的所有行
     * @return List对象  未处理数据的集合
     */
    public static List<String> getNotProRows(List<String> allRows) {
        List<String> notProList = new ArrayList<>();
        String tmp;
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
                tmp = allRows.get(i);
                String processState = getRowNumber(tmp);
                if (!processState.equals(getRowNumber(allRows.get(i - 1))) && !processState.equals(getRowNumber(allRows.get(i + 1)))) {
                    notProList.add(tmp);
                }
            }
            if (!getRowNumber(allRows.get(0)).equals(getRowNumber(allRows.get(1)))) {
                notProList.add(allRows.get(0));
            }
            if (!getRowNumber(allRows.get(allRows.size() - 1)).equals(getRowNumber(allRows.get(allRows.size() - 2)))) {
                notProList.add(allRows.get(allRows.size() - 1));
            }
            return notProList;
        }

    }


    /**
     * 获取一行日志中数据处理状态
     *
     * @param row 一行数据
     * @return String        数据在日志中行号
     */
    private static String getRowNumber(String row) {
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
     * @param row 一行数据
     * @return String        数据处理的状态
     */
    private static String getProcessState(String row) {
        String processState = "";
        String split = ",";
        if (row == null || row.length() == 0) {
            LOG.warn("This row of data is empty");
        } else {
            processState = row.substring(row.lastIndexOf(split) + 1);
        }
        return processState;
    }

    /**
     * 根据路径合并两个文件的内容
     *
     * @param fileReceivePath 日志路径
     * @param fileProcessPath 日志路径
     * @return List对象       合并后的结果
     */
    public static List<String> getAllRows(String fileReceivePath, String fileProcessPath) {
        List<String> rows = new ArrayList<>();
        File fileReceive = new File(fileReceivePath);
        File fileProcess = new File(fileProcessPath);
        BufferedReader readerFielReceive = null;
        BufferedReader readerProcess = null;
        if (!fileReceive.exists() || !fileProcess.exists()) {
            LOG.warn("file: " + fileReceive + " or " + fileProcess + "does not exits, please check it...");
            return rows;
        }
        try {
            readerFielReceive = new BufferedReader(new FileReader(fileReceive));
            String line;
            while ((line = readerFielReceive.readLine()) != null) {
                rows.add(line);
            }
            LOG.info("The Number of the lines of" + fileReceive + " is " + rows.size());
            readerProcess = new BufferedReader(new FileReader(fileProcess));
            while ((line = readerProcess.readLine()) != null) {
                rows.add(line);
            }
            LOG.info("The Merged Number of the lines of " + fileReceive + fileProcess + "is" + rows.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readerFielReceive != null && readerProcess != null) {
                try {
                    readerFielReceive.close();
                    readerProcess.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rows;
    }


    /**
     * 获取合并后日志中数据处理失败的集合
     *
     * @param allRows 日志合并后的所有行
     * @return List对象  合并后集合中数据处理失败的集合
     */
    public static List<String> getFailRows(List<String> allRows) {
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
                if (!processState.equals("0")) {
                    failList.add(tmp);
                }
            }
            return failList;
        }
    }

}

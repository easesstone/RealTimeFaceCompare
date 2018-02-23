package com.hzgc.collect.expand.merge;

import java.util.List;


/**
 * 数据封装类：FileFactory（马燊偲）
 * 成员变量含义：
 * processParentDir：process日志的根目录
 * writingLogFile：CommonConf中的logName（000000.log）
 *
 * allBackupLogAbsPath：process日志根目录下所有除了0000.log、最大日志、error日志以外，
 *                      所有其他可以移到备份目录的日志文件
 * allProcessLogAbsPath：process目录下，所有除了error.log以外的process日志
 *
 */
class FileFactory {

    private MergeUtil mergeUtil = new MergeUtil();

    private String processParentDir;
    private String writingLogFile;
    private List<String> allBackupLogAbsPath;
    private List<String> allProcessLogAbsPath;

    //有参构造函数，传入需处理的日志根路径，
    // 以及CommonConf中的logName（000000.log）
    FileFactory(String processLogDir, String writingLogFile) {
        this.processParentDir = processLogDir;
        this.writingLogFile = writingLogFile;
        setAllBackupLogAbsPath();
        setAllProcessLogAbsPath();
    }

    /*
     set方法
     */

    /**
     * 列出process目录下，所有除了error.log以外的process日志
     */
    private void setAllProcessLogAbsPath() {
        allProcessLogAbsPath = mergeUtil.listAllProcessLogAbsPath(processParentDir);
    }

    /**
     * 列出process日志根目录下所有除了0000.log、最大日志、error日志以外，
     * 所有其他可以移到备份目录的日志文件
     */
    private void setAllBackupLogAbsPath() {
        allBackupLogAbsPath = mergeUtil.listAllBackupLogAbsPath(processParentDir, writingLogFile);
    }

    /*
        get方法
         */
    List<String> getAllProcessLogAbsPath() {
        return allProcessLogAbsPath;
    }

    List<String> getAllBackupLogAbsPath() {
        return allBackupLogAbsPath;
    }

}

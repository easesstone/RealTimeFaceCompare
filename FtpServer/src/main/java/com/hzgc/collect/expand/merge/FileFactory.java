package com.hzgc.collect.expand.merge;

import java.util.List;


/**
 * 数据封装类：FileFactory（马燊偲）
 * 成员变量含义：
 * processParentDir：process日志的根目录
 * writingLogFile：CommonConf中的logName（000000.log）
 * allBackupLog：process日志根目录下所有除了0000.log、最大日志、error日志以外，
 *                      所有其他可以移到备份目录的日志文件
 * allFiles：process目录下，所有的文件
 */
class FileFactory {

    private  FileUtil fileUtil = new FileUtil();

    private  String processParentDir;
    private String writingLogFile;
    private  List<String> allBackupLog;
    private  List<String> allFiles;

    //有参构造函数，传入需处理的日志根路径，
    // 以及CommonConf中的logName（000000.log）
    FileFactory(String processLogDir, String writingLogFile) {
        this.processParentDir = processLogDir;
        this.writingLogFile = writingLogFile;
        setAllBackupLog();
    }

    /**
     * set方法
     */

    /**
     * 列出process日志根目录下所有文件
     */
    public void setAllFiles(List<String> allFiles) {
        allFiles = fileUtil.listAllFileAbsPath(processParentDir);
    }

    /**
     * 列出process日志根目录下所有除了0000.log、最大日志、error日志以外，
     * 所有其他可以移到备份目录的日志文件
     */
    private void setAllBackupLog() {
        allBackupLog = fileUtil.listAllBackupLogAbsPath(processParentDir, writingLogFile);
    }



    /**
     * get方法
     */
    public List<String> getAllFiles() {
        return allFiles;
    }

    List<String> getAllBackupLog() {
        return allBackupLog;
    }

}

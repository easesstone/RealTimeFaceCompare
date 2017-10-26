package com.hzgc.ftpserver.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import com.hzgc.ftpserver.pool.FTPClientConfigure;
import com.hzgc.ftpserver.pool.FtpClientFactory;
import org.apache.commons.net.ftp.*;
import org.apache.log4j.Logger;

public class Download {

    private static Logger logger = Logger.getLogger(Download.class);

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, String ftpUserName,
                                         String ftpPassword, int ftpPort) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX); //设置linux环境
            ftpClient.configure(conf);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) { //判断是否连接成功
                logger.info("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            } else {
                logger.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            logger.info("FTP的IP地址可能错误，请正确配置。");
            e.printStackTrace();
        } catch (IOException e) {
            logger.info("FTP的端口错误,请正确配置。");
            e.printStackTrace();
        }
        return ftpClient;
    }

    /*
     * 从FTP服务器下载文件
     *
     * @param ftpHost FTP IP地址
     *
     * @param ftpUserName FTP 用户名
     *
     * @param ftpPassword FTP用户名密码
     *
     * @param ftpPort FTP端口
     *
     * @param ftpFilePath FTP服务器中文件所在路径 格式： /3B0383FPAG00883/16/00
     *
     * @param ftpFileName 从FTP服务器中下载的文件名称
     *
     * @param localPath 下载到本地的位置 格式：D:/download
     *
     * @param localFileName 下载到本地的文件名称
     */
    public static void downloadFtpFile(String ftpHost, String ftpUserName,
                                       String ftpPassword, int ftpPort,
                                       String ftpFilePath, String ftpFileName,
                                       String localPath, String localFileName) {

        FTPClient ftpClient = null;

        try {
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpFilePath);//转移到FTP服务器目录

            File dir = new File(localPath);
            if (!dir.exists()) {//判断文件目录是否存在
                dir.mkdirs();
            }

            File localFile = new File(localPath + File.separatorChar + localFileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(ftpFileName, os);
            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            logger.error("没有找到" + ftpFilePath + "文件");
            e.printStackTrace();
        } catch (SocketException e) {
            logger.error("连接FTP失败.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("文件读取错误。");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String ftpHost = "192.168.1.28";
        String ftpUserName = "admin";
        String ftpPassword = "123456";
        int ftpPort = 2121;
        String ftpFilePath = "/3B0383FPAG00883/16/00";
        String ftpFileName = "2017_05_23_16_00_15_5704_0.jpg";
        String localPath = "F:\\data";
        String localFileName = "aaa.jpg";
        Download.downloadFtpFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpFilePath, ftpFileName, localPath, localFileName);
        System.out.println("ok");
    }
}
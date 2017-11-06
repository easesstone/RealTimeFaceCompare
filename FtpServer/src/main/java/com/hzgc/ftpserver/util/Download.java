package com.hzgc.ftpserver.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.SocketException;

/**
 * ftpClient文件下载
 */
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
                logger.info("Failed to connect to FTPClient, user name or password error.");
                ftpClient.disconnect();
            } else {
                logger.info("FTPClient connection successful.");
            }
        } catch (SocketException e) {
            logger.info("FTP IP address may be incorrect, please configure correctly.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.info("FTP port error, please configure correctly.");
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
            //连接FTPClient并转移到FTP服务器目录
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpFilePath);//转移到FTP服务器目录

            //判断文件目录是否存在
            File dir = new File(localPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File localFile = new File(localPath + File.separatorChar + localFileName);
            OutputStream os = new FileOutputStream(localFile);
            //通过FTPClient获取文件
            ftpClient.retrieveFile(ftpFileName, os);

            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            logger.error("Failed to find the " + ftpFilePath + " file below");
            e.printStackTrace();
        } catch (SocketException e) {
            logger.error("Failed to connect FTPClient.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("File read error.");
            e.printStackTrace();
        }
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpUserName   FTP 用户名
     * @param ftpPassword   FTP用户名密码
     * @param ftpUrl        FTP地址
     * @param localPath     下载到本地的位置 格式：D:/download
     * @param localFileName 下载到本地的文件名称
     */
    public static void downloadFtpFile(String ftpUserName, String ftpPassword, String ftpUrl, String localPath, String localFileName) {
        if (!ftpUrl.isEmpty() && !localPath.isEmpty() && !localFileName.isEmpty()) {
            //解析FTP地址，得到ftpAddress、ftpPort、ftpFilePath、ftpFileName
            String ftpAddress = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
            String path = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
            int ftpPort = Integer.parseInt(path.substring(0, path.indexOf("/")));
            String ftpFilePath = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            String ftpFileName = path.substring(path.lastIndexOf("/") + 1);
            downloadFtpFile(ftpAddress, ftpUserName, ftpPassword, ftpPort, ftpFilePath, ftpFileName, localPath, localFileName);
        }
    }

    /**
     * 从FTP服务器下载文件并转为字节数组
     *
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpUrl      FTP地址
     * @return 文件的字节数组
     */
    public static byte[] downloadftpFile2Bytes(String ftpUserName, String ftpPassword, String ftpUrl) {
        byte[] ftpFileBytes = null;
        if (!ftpUrl.isEmpty()) {
            //解析FTP地址，得到ftpAddress、ftpPort、ftpFilePath、ftpFileName
            String ftpAddress = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
            String path = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
            int ftpPort = Integer.parseInt(path.substring(0, path.indexOf("/")));
            String ftpFilePath = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            String ftpFileName = path.substring(path.lastIndexOf("/") + 1);

            FTPClient ftpClient;
            InputStream in;

            try {
                //连接FTPClient并转移到FTP服务器目录
                ftpClient = getFTPClient(ftpAddress, ftpUserName, ftpPassword, ftpPort);
                ftpClient.setControlEncoding("UTF-8"); // 中文支持
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory(ftpFilePath);//转移到FTP服务器目录

                //通过FTPClient获取文件输入流并转为byte[]
                in = ftpClient.retrieveFileStream(ftpFileName);
                ftpFileBytes = FtpUtil.inputStreamCacher(in).toByteArray();

                in.close();
                ftpClient.logout();

            } catch (FileNotFoundException e) {
                logger.error("Failed to find the " + ftpFilePath + " file below");
                e.printStackTrace();
            } catch (SocketException e) {
                logger.error("Failed to connect FTPClient.");
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("File read error.");
                e.printStackTrace();
            }
        } else {
            logger.warn("method param is error.");
        }
        return ftpFileBytes;
    }


    /*public static void main(String[] args) {
        String ftpAddress = "192.168.1.28";
        String ftpUserName = "admin";
        String ftpPassword = "123456";
        int ftpPort = 2121;
        String ftpFilePath = "/3B0383FPAG00883/2017/05/23/16/00/";
        String ftpFileName = "2017_05_23_16_00_15_5704_0.jpg";
        String localPath = "F:\\data";
        String localFileName = "aaa.jpg";
        String localFileName22 = "ddd.jpg";
        String ftpUrl = "ftp://192.168.1.28:2121/3B0383FPAG00883/2017/05/23/16/00/2017_05_23_16_00_15_5704_0.jpg";
        Download.downloadFtpFile(ftpAddress, ftpUserName, ftpPassword, ftpPort, ftpFilePath, ftpFileName, localPath, localFileName);
        Download.downloadFtpFile(ftpUserName, ftpPassword, ftpUrl, localPath, localFileName22);
        byte[] bytes = Download.downloadftpFile2Bytes(ftpUserName, ftpPassword, ftpUrl);
        System.out.println(Arrays.toString(bytes));
        System.out.println("ok");
    }*/
}
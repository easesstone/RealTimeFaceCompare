package com.hzgc.hbase.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;


public class FtpUtil {

    private static Logger logger = Logger.getLogger(FtpUtil.class);


    /**
     * 输入流转为字节数组
     *
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] inputStreamCacher(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IoUtils.close(baos);
            IoUtils.close(is);
        }
        return baos.toByteArray();
    }

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
                ftpFileBytes = inputStreamCacher(in);

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

    /**
     * 小图ftpUrl转大图ftpUrl
     *
     * @param surl 小图ftpUrl
     * @return 大图ftpUrl
     */
    public static String surlToBurl(String surl) {
        StringBuilder burl = new StringBuilder();
        String s1 = surl.substring(0, surl.lastIndexOf("_") + 1);
        String s2 = surl.substring(surl.lastIndexOf("."));
        burl.append(s1).append(0).append(s2);
        return burl.toString();
    }
}

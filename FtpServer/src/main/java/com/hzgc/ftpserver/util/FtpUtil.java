package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.local.FileType;
import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FtpUtil implements Serializable {
    private static Logger LOG = Logger.getLogger(FtpUtil.class);

    public static boolean checkPort(int checkPort) throws Exception {
        return checkPort > 1024;
    }

    public static ByteArrayOutputStream inputStreamCacher(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IoUtils.close(baos);
            IoUtils.close(is);
        }
        return baos;
    }

    /**
     * @param pictureName determine the picture type based on the file name
     * @return equals 0, it is a picture
     * lager than 0, it is a face picture
     */
    public static int pickPicture(String pictureName) {
        int picType = 0;
        if (null != pictureName) {
            String tmpStr = pictureName.substring(pictureName.lastIndexOf("_") + 1, pictureName.lastIndexOf("."));
            try {
                picType = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return picType;
    }

    /**
     * 根据文件上传至ftp的绝对路径获取到ipcID、TimeStamp、Date、TimeSlot
     *
     * @param fileName 文件上传至ftp的绝对路径，例如：/3B0383FPAG51511/2017/05/23/16/00/2017_05_23_16_00_15_5704_0.jpg
     * @return 设备、时间等信息 例如：{date=2017-05-23, sj=1600, ipcID=3B0383FPAG51511, time=2017-05-23 16:00:15}
     */
    public static Map<String, String> getFtpPathMessage(String fileName) {
        String ipcID = fileName.substring(1, fileName.indexOf("/", 1));
        String timeStr = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("_")).replace("_", "");

        String year = timeStr.substring(0, 4);
        String month = timeStr.substring(4, 6);
        String day = timeStr.substring(6, 8);
        String hour = timeStr.substring(8, 10);
        String minute = timeStr.substring(10, 12);
        String second = timeStr.substring(12, 14);

        StringBuilder time = new StringBuilder();
        time = time.append(year).append("-").append(month).append("-").append(day).
                append(" ").append(hour).append(":").append(minute).append(":").append(second);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder date = new StringBuilder();
        date = date.append(year).append("-").append(month).append("-").append(day);

        StringBuilder sj = new StringBuilder();
        sj = sj.append(hour).append(minute);

        Map<String, String> map = new HashMap<>();
        try {
            /*Date date = sdf.parse(time.toString());
            long timeStamp = date.getTime();*/
            map.put("ipcID", ipcID);
            map.put("time", time.toString());
            map.put("date", date.toString());
            map.put("sj", sj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据ftpUrl获取到IP、HostName、ipcID、TimeStamp、Date、TimeSlot
     *
     * @param ftpUrl ftp地址 例如：ftp://172.18.18.109:2121/ABCVS20160823CCCH/2017_11_09_10_53_35_2_0.jpg
     * @return 设备、时间等信息 例如：{date=2017-11-09, hostname=:2121, ip=172.18.18.109, timeslot=1053, ipcid=ABCVS20160823CCCH, timestamp=2017-11-09 10:53:35}
     */
    public static Map<String, String> getFtpUrlMessage(String ftpUrl) {
        Map<String, String> map = new HashMap<>();
        String ip = ftpUrl.substring(ftpUrl.indexOf(":") + 3, ftpUrl.lastIndexOf(":"));
        String hostnameStr = ftpUrl.substring(ftpUrl.lastIndexOf(":"));
        String hostName = hostnameStr.substring(0, hostnameStr.indexOf("/"));
        String filePath = hostnameStr.substring(hostnameStr.indexOf("/"));
        Map<String, String> filePathMap = getFtpPathMessage(filePath);
        if (!filePathMap.isEmpty()) {
            String ipcID = filePathMap.get("ipcID");
            String timeStamp = filePathMap.get("time");
            String date = filePathMap.get("date");
            String timeSlot = filePathMap.get("sj");
            map.put("ip", ip);
            map.put("hostname", hostName);
            map.put("filepath", filePath);
            map.put("ipcid", ipcID);
            map.put("timestamp", timeStamp);
            map.put("date", date);
            map.put("timeslot", timeSlot);
        }
        return map;
    }

    /**
     * 通过rowKey解析到文件保存的绝对路径
     *
     * @param rowKey rowKey
     * @param type   文件类型
     * @return 绝对路径
     */
    //TODO 后续需要通过ftp上传路径来解析，取消rowkey字段
    public static String key2absolutePath(String rowKey, FileType type) {
        StringBuilder url = new StringBuilder();

        String ipcId = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        String second = timeStr.substring(10, 12);

        String rowkey1 = rowKey.substring(0, rowKey.lastIndexOf("_"));
        String postId = rowkey1.substring(rowkey1.indexOf("_") + 14, rowkey1.lastIndexOf("_"));
        int numType = Integer.parseInt(rowkey1.substring(rowkey1.lastIndexOf("_") + 1, rowkey1.length()));

        String ftpServerIP = "";
        int ftpServerPort = 0;
        String hostName = rowKey.substring(rowKey.lastIndexOf("_") + 1, rowKey.length());
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
            properties.load(in);
            ftpServerPort = Integer.parseInt(properties.getProperty("port"));
            ftpServerIP = properties.getProperty(hostName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        url = url.append("ftp://").append(ftpServerIP).append(":").append(ftpServerPort).append("/").append(ipcId).
                append("/20").append(year).append("/").append(month).append("/").append(day).
                append("/").append(hour).append("/").
                append("20").append(year).append("_").append(month).append("_").append(day).
                append("_").append(hour).append("_").append(minute).append("_").append(second).
                append("_").append(postId);

        if (type == FileType.PICTURE) {
            url = url.append("_0").append(".jpg");
        } else if (type == FileType.FACE) {
            if (numType == 0) {
                LOG.info("picture rowKey cannot analysis to face filePath !");
            } else if (numType > 0) {
                url = url.append("_").append(numType).append(".jpg");
            } else {
                LOG.warn("rowKey format error :" + rowKey);
            }
        } else if (type == FileType.JSON) {
            url = url.append("_0").append(".json");
        }
        return url.toString();
    }

    /**
     * 通过上传文件路径解析到文件的ftp地址（ftp发送至kafka的key）
     *
     * @param filePath ftp接收数据路径
     * @return 文件的ftp地址
     */
    public static String filePath2absolutePath(String filePath) {
        StringBuilder url = new StringBuilder();
        int ftpServerPort = 0;
        String hostName = IpAddressUtil.getHostName();
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
            properties.load(in);
            ftpServerPort = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        url = url.append("ftp://").append(hostName).append(":").append(ftpServerPort).append(filePath);
        return url.toString();
    }

    /**
     * 通过rowKey解析文件保存相对路径
     *
     * @param rowKey rowKey
     * @return 相对路径
     */
    //TODO 后续需要通过ftp上传路径来解析，取消rowkey字段
    public static String key2relativePath(String rowKey) {
        StringBuilder filePath = new StringBuilder();

        String ipcId = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        //String second = timeStr.substring(10, 12);

        String ftpServerIP = "";
        int ftpServerPort = 0;
        String hostName = rowKey.substring(rowKey.lastIndexOf("_") + 1, rowKey.length());
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
            properties.load(in);
            ftpServerPort = Integer.parseInt(properties.getProperty("port"));
            ftpServerIP = properties.getProperty(hostName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(in);
        }

        filePath = filePath.append("ftp://").append(ftpServerIP).append(":").append(ftpServerPort).append("/").append(ipcId).
                append("/20").append(year).append("/").append(month).append("/").append(day).append("/").append(hour);
        return filePath.toString();
    }

    /**
     * 通过rowKey解析文件名称
     *
     * @param rowKey rowKey
     * @param type   文件类型
     * @return 文件名称
     */
    //TODO 后续需要通过ftp上传路径来解析，取消rowkey字段
    public static String key2fileName(String rowKey, FileType type) {
        StringBuilder fileName = new StringBuilder();

        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        String second = timeStr.substring(10, 12);

        String rowkey1 = rowKey.substring(0, rowKey.lastIndexOf("_"));
        String postId = rowkey1.substring(rowkey1.indexOf("_") + 14, rowkey1.lastIndexOf("_"));
        int numType = Integer.parseInt(rowkey1.substring(rowkey1.lastIndexOf("_") + 1, rowkey1.length()));

        fileName = fileName.append("20").append(year).append("_").append(month)
                .append("_").append(day).append("_").append(hour)
                .append("_").append(minute).append("_").append(second).append("_").append(postId);

        if (type == FileType.PICTURE) {
            fileName = fileName.append("_0").append(".jpg");
        } else if (type == FileType.FACE) {
            if (numType == 0) {
                LOG.info("picture rowKey cannot analysis to face fileName !");
            } else if (numType > 0) {
                fileName = fileName.append("_").append(numType).append(".jpg");
            } else {
                LOG.warn("rowKey format error :" + rowKey);
            }
        } else if (type == FileType.JSON) {
            fileName = fileName.append("_0").append(".json");
        } else {
            LOG.warn("method param is error.");
        }
        return fileName.toString();
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
                LOG.info("Failed to connect to FTPClient, user name or password error.");
                ftpClient.disconnect();
            } else {
                LOG.info("FTPClient connection successful.");
            }
        } catch (SocketException e) {
            LOG.info("FTP IP address may be incorrect, please configure correctly.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.info("FTP port error, please configure correctly.");
            e.printStackTrace();
        }
        return ftpClient;
    }

    /**
     * 从FTP服务器下载文件并转为字节数组
     *
     * @param ftpUrl      FTP地址
     * @return 文件的字节数组
     */
    public static byte[] downloadftpFile2Bytes(String ftpUrl) {
        byte[] ftpFileBytes = null;
        if (!ftpUrl.isEmpty()) {
            //解析FTP地址，得到ftpAddress、ftpPort、ftpFilePath、ftpFileName
            String ftpAddress = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
            String path = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
            int ftpPort = Integer.parseInt(path.substring(0, path.indexOf("/")));
            String ftpFilePath = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            String ftpFileName = path.substring(path.lastIndexOf("/") + 1);

            //通过ftpAddress.properties配置文件，ftpUserName、ftpPassword
            String ftpUserName = "";
            String ftpPassword = "";
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
                properties.load(inputStream);
                ftpUserName = properties.getProperty("user");
                ftpPassword = properties.getProperty("password");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                IOUtil.closeStream(inputStream);
            }

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
                ftpFileBytes = inputStreamCacher(in).toByteArray();

                in.close();
                ftpClient.logout();

            } catch (FileNotFoundException e) {
                LOG.error("Failed to find the " + ftpFilePath + " file below");
                e.printStackTrace();
            } catch (SocketException e) {
                LOG.error("Failed to connect FTPClient.");
                e.printStackTrace();
            } catch (IOException e) {
                LOG.error("File read error.");
                e.printStackTrace();
            }
        } else {
            LOG.warn("method param is error.");
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

    /**
     * ftpUrl中的HostName转为IP
     *
     * @param ftpUrl 带HostName的ftpUrl
     * @return 带IP的ftpUrl
     */
    public static String getFtpUrl(String ftpUrl){
        String url;
        String hostName = ftpUrl.substring(ftpUrl.indexOf("/") + 2 , ftpUrl.lastIndexOf(":"));
        Properties properties = new Properties();
        InputStream in = null;
        String ftpServerIP = "";
        try {
            in = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
            properties.load(in);
            ftpServerIP = properties.getProperty(hostName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(in);
        }
        url = ftpUrl.replace(hostName,ftpServerIP);
        return url;
    }
}

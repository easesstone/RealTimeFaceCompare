package com.hzgc.collect.expand.processer;

import java.io.Serializable;

public class FtpUrlMessage extends FtpPathMessage implements Serializable {
    private String ip;
    private String port;
    private String filePath;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

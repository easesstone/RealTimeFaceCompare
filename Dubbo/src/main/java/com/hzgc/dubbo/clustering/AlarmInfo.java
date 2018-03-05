package com.hzgc.dubbo.clustering;

import java.io.Serializable;

/**
 * 新增告警信息，包含大小图url，告警时间
 */
public class AlarmInfo implements Serializable {
    /**
     * 小图URL
     */
    private String surl;
    /**
     * 大图URL
     */
    private String burl;
    /**
     * 告警时间
     */
    private String alarmTime;

    public String getSurl() {
        return surl;
    }

    public void setSurl(String surl) {
        this.surl = surl;
    }

    public String getBurl() {
        return burl;
    }

    public void setBurl(String burl) {
        this.burl = burl;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
}

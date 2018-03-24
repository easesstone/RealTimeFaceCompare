package com.hzgc.dubbo.address;

import java.util.List;

/**
 * 人脸抓拍演示功能（过滤前端设备）
 */
public interface FtpShow {
    /**
     * 打开人脸抓拍演示功能
     *
     * @param userId 用户ID
     * @param ipcIdList 设备列表
     */
    public void openFtpShow(String userId, List<String> ipcIdList);

    /**
     * 关闭人脸抓拍演示功能
     *
     * @param userId 用户ID
     */
    public void closeFtpShow(String userId);

    /**
     * 查询需要演示的设备列表
     *
     * @return 设备列表
     */
    public List<String> getIpcId();
}

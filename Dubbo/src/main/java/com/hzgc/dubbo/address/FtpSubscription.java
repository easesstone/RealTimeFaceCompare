package com.hzgc.dubbo.address;

import java.util.List;

/**
 * 人脸抓拍订阅功能（过滤前端设备）
 */
public interface FtpSubscription {
    /**
     * 打开人脸抓拍订阅功能
     *
     * @param userId    用户ID
     * @param ipcIdList 设备ID列表
     */
    void openFtpReception(String userId, List<String> ipcIdList);

    /**
     * 关闭人脸抓拍订阅功能
     *
     * @param userId 用户ID
     */
    void closeFtpReception(String userId);
}

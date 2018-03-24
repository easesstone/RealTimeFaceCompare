package com.hzgc.service.address;

import com.hzgc.collect.expand.subscribe.ZookeeperParam;
import com.hzgc.collect.expand.util.ZookeeperClient;
import com.hzgc.dubbo.address.FtpSubscription;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

public class FtpSubscriptionImpl implements FtpSubscription, Serializable {
    private static Logger LOG = Logger.getLogger(FtpSubscriptionImpl.class);
    private ZookeeperClient zookeeperClient;

    public FtpSubscriptionImpl() {
        zookeeperClient = new ZookeeperClient(ZookeeperParam.SESSION_TIMEOUT, ZookeeperParam.zookeeperAddress,
                ZookeeperParam.PATH_SUBSCRIBE, ZookeeperParam.WATCHER);
    }

    /**
     * 打开MQ接收数据
     *
     * @param userId    用户ID
     * @param ipcIdList 设备ID列表
     */
    @Override
    public void openFtpReception(String userId, List<String> ipcIdList) {
        if (!userId.equals("") && !ipcIdList.isEmpty()) {
            String childPath = ZookeeperParam.PATH_SUBSCRIBE + "/" + userId;
            long time = System.currentTimeMillis();
            StringBuilder data = new StringBuilder();
            data.append(userId).append(",").append(time).append(",");
            for (String ipcId : ipcIdList) {
                data.append(ipcId).append(",");
            }
            zookeeperClient.create(childPath, data.toString().getBytes());
        }
    }

    /**
     * 关闭MQ接收数据
     *
     * @param userId 用户ID
     */
    @Override
    public void closeFtpReception(String userId) {
        if (!userId.equals("")) {
            zookeeperClient.delete(ZookeeperParam.PATH_SUBSCRIBE + "/" + userId);
        }
    }
}

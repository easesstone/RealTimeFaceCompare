package com.hzgc.service.address;

import com.hzgc.collect.expand.subscribe.FtpShowClient;
import com.hzgc.collect.expand.subscribe.ZookeeperParam;
import com.hzgc.collect.expand.util.ZookeeperClient;
import com.hzgc.dubbo.address.FtpShow;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FtpShowImpl implements FtpShow, Serializable {
    private static Logger LOG = Logger.getLogger(FtpShowImpl.class);
    private ZookeeperClient zookeeperClient;

    public FtpShowImpl() {
        zookeeperClient = new ZookeeperClient(ZookeeperParam.SESSION_TIMEOUT,
                ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_SHOW, ZookeeperParam.WATCHER);
    }

    /**
     * 打开MQ演示功能
     *
     * @param userId
     * @param ipcIdList 设备列表
     */
    @Override
    public void openFtpShow(String userId, List<String> ipcIdList) {
        if (!userId.equals("") && !ipcIdList.isEmpty()) {
            String childPath = ZookeeperParam.PATH_SHOW + "/" + userId;
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
     * 关闭MQ演示功能
     *
     * @param userId
     */
    @Override
    public void closeFtpShow(String userId) {
        if (!userId.equals("")) {
            zookeeperClient.delete(ZookeeperParam.PATH_SHOW + "/" + userId);
        }
    }

    /**
     * 查询需要演示的设备列表
     *
     * @return 设备列表
     */
    @Override
    public List<String> getIpcId() {
        List<String> ipcIdList = new ArrayList<>();
        FtpShowClient ftpShowClient = new FtpShowClient(ZookeeperParam.SESSION_TIMEOUT, ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_SHOW, false);
        ftpShowClient.createConnection(ZookeeperParam.zookeeperAddress, ZookeeperParam.SESSION_TIMEOUT);
        List<String> children = ftpShowClient.getChildren();
        if (!children.isEmpty()) {
            for (String child : children) {
                String childPath = ZookeeperParam.PATH_SHOW + "/" + child;
                byte[] data = ftpShowClient.getDate(childPath);
                if (data != null) {
                    String ipcIds = new String(data);
                    if (!ipcIds.equals("") && ipcIds.contains(",") && ipcIds.split(",").length >= 3) {
                        ipcIds = ipcIds.substring(0, ipcIds.length() - 1);
                        List<String> list = Arrays.asList(ipcIds.split(","));
                        ipcIdList = new ArrayList<>();
                        for (int i = 2; i < list.size(); i++) {
                            ipcIdList.add(list.get(i));
                        }
                    }
                }
            }
        }
        return ipcIdList;
    }
}

package com.hzgc.collect.expand.subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订阅与演示对象
 */
public class ReceiveIpcIds implements Serializable {

    //zookeeper中保存的抓拍订阅设备信息
    Map<String, Map<String, List<String>>> map_ZKData;

    //订阅功能设备列表
    private volatile List<String> ipcIdList_subscription;

    //演示功能设备列表
    private volatile List<String> ipcIdList_show;

    private static ReceiveIpcIds instance = null;

    ReceiveIpcIds() {
    }

    public static ReceiveIpcIds getInstance() {
        if (instance == null) {
            synchronized (ReceiveIpcIds.class) {
                if (instance == null) {
                    instance = new ReceiveIpcIds();
                }
            }
        }
        return instance;
    }

    public List<String> getIpcIdList_subscription() {
        return ipcIdList_subscription;
    }

    public void setIpcIdList_subscription(List<String> ipcIdList_subscription) {
        this.ipcIdList_subscription = ipcIdList_subscription;
    }

    public void setIpcIdList_subscription(Map<String, Map<String, List<String>>> map) {
        this.ipcIdList_subscription = setIpcIdList(map);
    }

    public List<String> getIpcIdList_show() {
        return ipcIdList_show;
    }

    public void setIpcIdList_show(List<String> ipcIdList_show) {
        this.ipcIdList_show = ipcIdList_show;
    }

    public void setIpcIdList_show(Map<String, Map<String, List<String>>> map) {
        this.ipcIdList_show = setIpcIdList(map);
    }

    private List<String> setIpcIdList(Map<String, Map<String, List<String>>> map) {
        List<String> ipcIdList = new ArrayList<>();
        if (!map.isEmpty()) {
            for (String userId : map.keySet()) {
                if (userId != null && !userId.equals("")) {
                    Map<String, List<String>> map1 = map.get(userId);
                    if (!map1.isEmpty()) {
                        for (String time : map1.keySet()) {
                            if (time != null && !time.equals("")) {
                                ipcIdList.addAll(map1.get(time));
                            }
                        }
                    }
                }
            }
        }
        return ipcIdList;
    }
}

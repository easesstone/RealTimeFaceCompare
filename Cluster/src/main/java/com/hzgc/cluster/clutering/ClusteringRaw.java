package com.hzgc.cluster.clutering;


import scala.collection.immutable.List;

public class ClusteringRaw {
    private String firstUrl;
    private List<String> dataList;

    public String getFirstUrl() {
        return firstUrl;
    }

    public void setFirstUrl(String firstUrl) {
        this.firstUrl = firstUrl;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }
}

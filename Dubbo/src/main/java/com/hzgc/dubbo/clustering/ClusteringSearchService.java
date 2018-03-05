package com.hzgc.dubbo.clustering;


import java.util.List;

public interface ClusteringSearchService {
    /**
     * 查询聚类信息
     *
     * @param time      聚类时间
     * @param start     返回数据下标开始符号
     * @param limit     行数
     * @param sortParam 排序参数（默认按出现次数排序）
     * @return 聚类列表
     */
    List<ClusteringAttribute> ClusteringSearch(String time, int start, int limit, String sortParam);

    /**
     * 查询单个聚类详细信息(告警记录)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    List<AlarmInfo> DetailClusteringSearch(String clusterId, String time, int start, int limit, String sortParam);

    /**
     * 查询单个聚类详细信息(告警ID)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    List<Integer> DetailClusteringSearch_v1(String clusterId, String time, int start, int limit, String sortParam);
}

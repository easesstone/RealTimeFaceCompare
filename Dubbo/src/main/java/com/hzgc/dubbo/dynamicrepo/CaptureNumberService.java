package com.hzgc.dubbo.dynamicrepo;

import java.util.List;
import java.util.Map;

public interface CaptureNumberService {
    String totolNum ="totolNumber";
    String todyTotolNumber ="todyTotolNumber";

    /**
     * 查询es的动态库，返回总抓拍数量和今日抓拍数量
     * @param ipcId 设备ID：ipcId
     * @return 返回总抓拍数和今日抓拍数量
     */
    Map<String,Integer> DynaicNumberService(List<String> ipcId);

    /**
     * 查询es的静态库，返回对应platformId下的pkey的个数
     * @param platformId 平台ID
     * @return 返回对应平台下的每个pkey的数量
     */
    Map<String,Integer> staticNumberService(String platformId);


    /**
     * 根据入参ipcid的list、startTime和endTime去es查询到相应的值
     *
     * @param ipcId 设备ID
     * @param startTime 搜索的开始时间
     * @param endTime 搜索的结束时间
     * @return 返回对应这些设备ID的这段时间内每一小时抓拍的总数量
     */
    Map<String,Integer> timeSoltNumber(List<String> ipcId, String startTime, String endTime);
}

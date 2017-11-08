package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.Attribute.*;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.TimeInterval;

import java.text.SimpleDateFormat;


class ParseByOption {

    /***
     * 获取拼接sql
     * @param searchFeaStr 特征值
     * @param option 搜索条件
     * @return 返回拼接的sql
     */
    String getSQLwithOption(String searchFeaStr, SearchOption option) {

        //date分区字段
        SimpleDateFormat dateFormat_date = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("select * from ").append("( select * , ")
                .append(DynamicTable.FUNCTION_NAME).append("(")
                .append("'").append(searchFeaStr).append("'").append(",")
                .append(DynamicTable.FEATURE).append(") as")
                .append(DynamicTable.SIMILARITY).append(" from ")
                .append(DynamicTable.PERSON_TABLE).append(") person_temp_table ");
        //判断阈值
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicTable.SIMILARITY)
                    .append(" >= ")
                    .append(option.getThreshold());
        }
        //判断人脸对象属性
        if (option.getAttributes() != null) {
            for (Attribute attribute : option.getAttributes()) {
                if (attribute.getValues() != null) {
                    for (AttributeValue attributeValue : attribute.getValues()) {
                        if (String.valueOf(attribute.getLogistic()).equals("AND")) {
                            stringBuilder
                                    .append(" and ")
                                    .append("'")
                                    .append(attribute.getIdentify())
                                    .append("'")
                                    .append(" = ")
                                    .append("'")
                                    .append(attributeValue.getValue())
                                    .append("'");
                        } else {
                            stringBuilder
                                    .append(" or ")
                                    .append("'")
                                    .append(attribute.getIdentify())
                                    .append("'")
                                    .append(" = ")
                                    .append("'")
                                    .append(attributeValue.getValue())
                                    .append("'");
                        }
                    }
                }
            }
        }
        //判断一个或多个时间区间 数据格式 小时+分钟 例如:1122
        stringBuilder.append(" and ");
        if (option.getIntervals() != null) {
            for (int i = 0; option.getIntervals().size() > i; i++) {
                TimeInterval timeInterval = option.getIntervals().get(i);
                int start_sj = timeInterval.getStart();
                String start_st = String.valueOf((start_sj / 60) * 100 + start_sj % 60);
                int end_sj = timeInterval.getEnd();
                String end_st = String.valueOf((end_sj / 60) * 100 + end_sj % 60);
                if (option.getIntervals().size() - 1 > i) {
                    stringBuilder
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append("'")
                            .append(start_st)
                            .append("'")
                            .append(" and ")
                            .append("'")
                            .append(end_st)
                            .append("'")
                            .append(" or ");
                } else {
                    stringBuilder
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append("'")
                            .append(start_st)
                            .append("'")
                            .append(" and ")
                            .append("'")
                            .append(end_st)
                            .append("'");
                }
            }
        }
        //判断开始时间和结束时间 数据格式 年-月-日 时:分:秒
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicTable.TIMESTAMP)
                    .append(" between ")
                    .append("'")
                    .append(option.getStartDate())
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(option.getEndDate())
                    .append("'");
        }
        //判断日期分区 数据格式 年-月-日
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicTable.DATE)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_date.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_date.format(option.getEndDate()))
                    .append("'");
        }
        //判断一个或多个设备id
        stringBuilder.append(" and ");
        if (option.getDeviceIds() != null) {
            for (int i = 0; option.getDeviceIds().size() > i; i++) {
                String ipcid = option.getDeviceIds().get(i);
                if (option.getDeviceIds().size() - 1 > i) {
                    stringBuilder
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'")
                            .append(" or ");
                } else {
                    stringBuilder
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'");
                }
            }
        }
        //合并两个表的结果集
        stringBuilder
                .append(" union all ");
        //查询临时表
        stringBuilder
                .append(" select * from ")
                .append("( select * ,").append(DynamicTable.FUNCTION_NAME)
                .append("(").append("'").append(searchFeaStr).append("'")
                .append(",").append(DynamicTable.FEATURE).append(") as ")
                .append(DynamicTable.SIMILARITY).append(" from ")
                .append(DynamicTable.MID_TABLE).append(") mid_temp_table ");
        //判断阈值
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicTable.SIMILARITY)
                    .append(" >= ")
                    .append(option.getThreshold());
        }
        //判断一个或多个人脸对象属性
        if (option.getAttributes() != null) {
            for (Attribute attribute : option.getAttributes()) {
                if (attribute.getValues() != null) {
                    for (AttributeValue attributeValue : attribute.getValues()) {
                        if (String.valueOf(attribute.getLogistic()).equals("AND")) {
                            stringBuilder
                                    .append(" and ")
                                    .append("'")
                                    .append(attribute.getIdentify())
                                    .append("'")
                                    .append(" = ")
                                    .append("'")
                                    .append(attributeValue.getValue())
                                    .append("'");
                        } else {
                            stringBuilder
                                    .append(" or ")
                                    .append("'")
                                    .append(attribute.getIdentify())
                                    .append("'")
                                    .append(" = ")
                                    .append("'")
                                    .append(attributeValue.getValue())
                                    .append("'");
                        }
                    }
                }
            }
        }
        //判断一个或多个时间区间 数据格式 小时+分钟 例如:1122
        stringBuilder.append(" and ");
        if (option.getIntervals() != null) {
            for (int i = 0; option.getIntervals().size() > i; i++) {
                TimeInterval timeInterval = option.getIntervals().get(i);
                int start_sj = timeInterval.getStart();
                String start_st = String.valueOf((start_sj / 60) * 100 + start_sj % 60);
                int end_sj = timeInterval.getEnd();
                String end_st = String.valueOf((end_sj / 60) * 100 + end_sj % 60);
                if (option.getIntervals().size() - 1 > i) {
                    stringBuilder
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append("'")
                            .append(start_st)
                            .append("'")
                            .append(" and ")
                            .append("'")
                            .append(end_st)
                            .append("'")
                            .append(" or ");
                } else {
                    stringBuilder
                            .append(DynamicTable.TIMESLOT)
                            .append(" between ")
                            .append("'")
                            .append(start_st)
                            .append("'")
                            .append(" and ")
                            .append("'")
                            .append(end_st)
                            .append("'");
                }
            }
        }
        //判断开始时间和结束时间 数据格式 年-月-日 时:分:秒
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicTable.TIMESTAMP)
                    .append(" between ")
                    .append("'")
                    .append(option.getStartDate())
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(option.getEndDate())
                    .append("'");
        }
        //判断日期分区 数据格式 年-月-日
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicTable.DATE)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_date.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_date.format(option.getEndDate()))
                    .append("'");
        }
        //判断一个或多个设备id
        stringBuilder.append(" and ");
        if (option.getDeviceIds() != null) {
            for (int i = 0; option.getDeviceIds().size() > i; i++) {
                String ipcid = option.getDeviceIds().get(i);
                if (option.getDeviceIds().size() - 1 > i) {
                    stringBuilder
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'")
                            .append(" or ");
                } else {
                    stringBuilder
                            .append(DynamicTable.IPCID)
                            .append(" = ")
                            .append("'")
                            .append(ipcid)
                            .append("'");
                }
            }
        }
        return stringBuilder.toString();
    }
}

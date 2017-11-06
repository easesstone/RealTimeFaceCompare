package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.Attribute.*;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.TimeInterval;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class FilterByOption {

    /***
     * 获取拼接sql
     * @param searchFeaStr 特征值
     * @param option 搜索条件
     * @return 返回拼接的sql
     */
    public String getSQLwithOption(String searchFeaStr, SearchOption option) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = option.getStartDate();
        Date endTime = option.getEndDate();
        String start = dateFormat.format(startTime);
        String startdate = start.substring(0,10);
        String end = dateFormat.format(endTime);
        String enddate = end.substring(0,10);
        //List<String> date = option.getDate();
        Attribute attribute = option.getAttribute();
        List<String> ipcId = option.getDeviceIds();
        //TimeIntervals 时间段的封装类
        TimeInterval timeInterval;
        List<TimeInterval> timeIntervals = option.getIntervals();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("select * from ")
                .append("( select * , ")
                .append("com").append("(").append("'").append(searchFeaStr).append("'").append(",")
                .append(DynamicHiveTable.FEATURE)
                .append(") as").append(DynamicHiveTable.SIMILARITY)
                .append(" from ")
                .append(DynamicHiveTable.PERSON_TABLE)
                .append(") person_temp_table ");
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicHiveTable.SIMILARITY)
                    .append(">=")
                    .append(option.getThreshold());
        }
        if (startTime != null && endTime != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIMESTAMP)
                    .append(" between ").append("'").append(start).append("'")
                    .append(" and ").append("'").append(end).append("'");
        }
        if (attribute.getEyeglasses().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.EYEGLASSES)
                    .append(" = ")
                    .append(attribute.getEyeglasses().getValue());
        }
        if (attribute.getGender().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.GENDER)
                    .append(" = ")
                    .append(attribute.getGender().getValue());
        }
        if (attribute.getHairColor().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRCOLOR)
                    .append(" = ")
                    .append(attribute.getHairColor().getValue());
        }
        if (attribute.getHairStyle().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRSTYLE)
                    .append(" = ")
                    .append(attribute.getHairStyle().getValue());
        }
        if (attribute.getHat().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAT)
                    .append(" = ")
                    .append(attribute.getHat().getValue());
        }
        if (attribute.getHuzi().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HUZI)
                    .append(" = ")
                    .append(attribute.getHuzi().getValue());
        }
        if (attribute.getTie().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIE)
                    .append(" = ")
                    .append(attribute.getTie().getValue());
        }
        if (timeIntervals != null) {
            for (TimeInterval timeInterval1 : timeIntervals) {
                timeInterval = timeInterval1;
                int start_sj = timeInterval.getStart();
                start_sj = start_sj / 60 + start_sj % 60;
                int end_sj = timeInterval.getEnd();
                end_sj = end_sj / 60 + end_sj % 60;
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.TIMESLOT)
                        .append(" between ")
                        .append("'")
                        .append(start_sj)
                        .append("'")
                        .append(" and ")
                        .append("'")
                        .append(end_sj)
                        .append("'");
            }
        }
        if (option.getDeviceIds() != null) {
            for (Object t :  option.getDeviceIds()) {
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.PARTITION_IPCID)
                        .append(" = ")
                        .append("'")
                        .append(t)
                        .append("'");
            }
        }
//        if (date != null) {
//            for (Object t : ipcId) {
//                stringBuilder
//                        .append(" and ")
//                        .append(DynamicHiveTable.PARTITION_DATE)
//                        .append(" = ").append("'").append(t).append("'");
//            }
//        }
        //合并两个表的结果集
        stringBuilder
                .append(" union all ");

        stringBuilder
                .append(" select * from ")
                .append("( select * ,")
                .append(DynamicHiveTable.FUNCTION_NAME)
                .append("(").append(searchFeaStr).append(",").append(DynamicHiveTable.FEATURE)
                .append(") as ").append(DynamicHiveTable.SIMILARITY)
                .append(" from ").append(DynamicHiveTable.MID_TABLE)
                .append(") mid_temp_table ");
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicHiveTable.SIMILARITY)
                    .append(">=")
                    .append(option.getThreshold());
        }
        if (startTime != null && endTime != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIMESTAMP)
                    .append(" between ").append("'").append(start).append("'")
                    .append(" and ").append("'").append(end).append("'");
        }
        if (attribute.getEyeglasses().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.EYEGLASSES)
                    .append(" = ")
                    .append(attribute.getGender().getValue());
        }
        if (attribute.getGender().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.GENDER)
                    .append(" = ")
                    .append(attribute.getGender().getValue());
        }
        if (attribute.getHairColor().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRCOLOR)
                    .append(" = ")
                    .append(attribute.getHairColor().getValue());
        }
        if (attribute.getHairStyle().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRSTYLE)
                    .append(" = ")
                    .append(attribute.getHairStyle().getValue());
        }
        if (attribute.getHat().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAT)
                    .append(" = ")
                    .append(attribute.getHat().getValue());
        }
        if (attribute.getHuzi().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HUZI)
                    .append(" = ")
                    .append(attribute.getHuzi().getValue());
        }
        if (attribute.getTie().getValue() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIE)
                    .append(" = ")
                    .append(attribute.getTie().getValue());
        }
        if (timeIntervals != null) {
            for (TimeInterval timeInterval1 : timeIntervals) {
                timeInterval = timeInterval1;
                int start_sj = timeInterval.getStart();
                start_sj = start_sj / 60 + start_sj % 60;
                int end_sj = timeInterval.getEnd();
                end_sj = end_sj / 60 + end_sj % 60;
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.TIMESLOT)
                        .append(" between ")
                        .append("'")
                        .append(start_sj)
                        .append("'")
                        .append(" and ")
                        .append("'")
                        .append(end_sj)
                        .append("'");
            }
        }
        if ( option.getDeviceIds() != null) {
            for (Object t :  option.getDeviceIds()) {
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.PARTITION_IPCID)
                        .append(" = ").append("'").append(t).append("'");
            }
        }
//        if (date != null) {
//            for (Object t : ipcId) {
//                stringBuilder.append(" and ")
//                        .append(DynamicHiveTable.PARTITION_DATE)
//                        .append(" = ").append("'").append(t).append("'");
//            }
//        }

        return stringBuilder.toString();
    }

}

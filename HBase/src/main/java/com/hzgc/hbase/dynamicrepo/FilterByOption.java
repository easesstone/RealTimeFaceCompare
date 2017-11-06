package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.Attribute.*;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.TimeInterval;

import java.text.SimpleDateFormat;


class FilterByOption {

    /***
     * 获取拼接sql
     * @param searchFeaStr 特征值
     * @param option 搜索条件
     * @return 返回拼接的sql
     */
    String getSQLwithOption(String searchFeaStr, SearchOption option) {

        //timestamp字段
        SimpleDateFormat dateFormat_timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //date分区字段
        SimpleDateFormat dateFormat_date = new SimpleDateFormat("yyyy-MM-dd");
        Attribute attribute = option.getAttribute();
        //TimeIntervals 时间段的封装类
        TimeInterval timeInterval;
        StringBuilder stringBuilder = new StringBuilder();
        /*
        完整的sql语句
        select * from ( select * ,compre('',feature) as similarity from person_table) person_temp_table
        where similarity >= threshold  and eyeglasses = ? and gender = ?
        and haircolor = ? and hairstytle = ? and hat = ? and huzi = ? tie = ?
        and timestamp between 'start_timestamp' and 'end_timestamp'
        and timesolt between 'start_timesolt' and 'end_timesolt'
        and  date between 'start_date' and 'end_date'
        and ipcid = ?
        union all
        select * from ( select * ,compre('',feature) as similarity from mid_table) mid_temp_table
        where similarity >= threshold  and eyeglasses = ? and gender = ?
        and haircolor = ? and hairstytle = ? and hat = ? and huzi = ? tie = ?
        and timestamp between 'start_timestamp' and 'end_timestamp'
        and timesolt between 'start_timesolt' and 'end_timesolt'
        and  date between 'start_date' and 'end_date'
        and ipcid = ?;
         */
        stringBuilder
                .append("select * from ")
                .append("( select * , ")
                .append(DynamicHiveTable.FUNCTION_NAME)
                .append("(").append("'").append(searchFeaStr).append("'").append(",")
                .append(DynamicHiveTable.FEATURE)
                .append(") as").append(DynamicHiveTable.SIMILARITY)
                .append(" from ")
                .append(DynamicHiveTable.PERSON_TABLE)
                .append(") person_temp_table ");
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicHiveTable.SIMILARITY)
                    .append(" >= ")
                    .append(option.getThreshold());
        }
        if (attribute.getEyeglasses() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.EYEGLASSES)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getEyeglasses().getValue())
                    .append("'");
        }
        if (attribute.getGender() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.GENDER)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getGender().getValue())
                    .append("'");
        }
        if (attribute.getHairColor() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRCOLOR)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHairColor().getValue())
                    .append("'");
        }
        if (attribute.getHairStyle() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRSTYLE)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHairStyle().getValue())
                    .append("'");
        }
        if (attribute.getHat() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAT)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHat().getValue())
                    .append("'");
        }
        if (attribute.getHuzi() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HUZI)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHuzi().getValue())
                    .append("'");
        }
        if (attribute.getTie() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIE)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getTie().getValue())
                    .append("'");
        }
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIMESTAMP)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_timestamp.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_timestamp.format(option.getEndDate()))
                    .append("'");
        }
        if (option.getIntervals() != null) {
            for (TimeInterval timeInterval1 : option.getIntervals()) {
                timeInterval = timeInterval1;
                int start_sj = timeInterval.getStart();
                start_sj = (start_sj / 60) * 100 + start_sj % 60;
                int end_sj = timeInterval.getEnd();
                end_sj = (end_sj / 60) * 100 + end_sj % 60;
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.TIMESLOT)
                        .append(" between ")
                        .append(start_sj)
                        .append(" and ")
                        .append(end_sj);
            }
        }
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.PARTITION_DATE)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_date.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_date.format(option.getEndDate()))
                    .append("'");
        }
        if (option.getDeviceIds() != null) {
            for (String ipcid :  option.getDeviceIds()) {
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.PARTITION_IPCID)
                        .append(" = ")
                        .append("'")
                        .append(ipcid)
                        .append("'");
            }
        }
        //合并两个表的结果集
        stringBuilder
                .append(" union all ");

        stringBuilder
                .append(" select * from ")
                .append("( select * ,")
                .append(DynamicHiveTable.FUNCTION_NAME)
                .append("(").append("'").append(searchFeaStr).append("'").append(",").append(DynamicHiveTable.FEATURE)
                .append(") as ").append(DynamicHiveTable.SIMILARITY)
                .append(" from ").append(DynamicHiveTable.MID_TABLE)
                .append(") mid_temp_table ");
        if (0.00f != option.getThreshold()) {
            stringBuilder
                    .append(" where ")
                    .append(DynamicHiveTable.SIMILARITY)
                    .append(" >= ")
                    .append(option.getThreshold());
        }

        if (attribute.getEyeglasses() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.EYEGLASSES)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getEyeglasses().getValue())
                    .append("'");
        }
        if (attribute.getGender() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.GENDER)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getGender().getValue())
                    .append("'");
        }
        if (attribute.getHairColor() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRCOLOR)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHairColor().getValue())
                    .append("'");
        }
        if (attribute.getHairStyle() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAIRSTYLE)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHairStyle().getValue())
                    .append("'");
        }
        if (attribute.getHat() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HAT)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHat().getValue())
                    .append("'");
        }
        if (attribute.getHuzi() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.HUZI)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getHuzi().getValue())
                    .append("'");
        }
        if (attribute.getTie() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIE)
                    .append(" = ")
                    .append("'")
                    .append(attribute.getTie().getValue())
                    .append("'");
        }
        if (option.getIntervals() != null) {
            for (TimeInterval timeInterval1 : option.getIntervals()) {
                timeInterval = timeInterval1;
                int start_sj = timeInterval.getStart();
                start_sj = (start_sj / 60) * 100 + start_sj % 60;
                int end_sj = timeInterval.getEnd();
                end_sj = (end_sj / 60) * 100+ end_sj % 60;
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.TIMESLOT)
                        .append(" between ")
                        .append(start_sj)
                        .append(" and ")
                        .append(end_sj);
            }
        }
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.TIMESTAMP)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_timestamp.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_timestamp.format(option.getEndDate()))
                    .append("'");
        }
        if (option.getStartDate() != null && option.getEndDate() != null) {
            stringBuilder
                    .append(" and ")
                    .append(DynamicHiveTable.PARTITION_DATE)
                    .append(" between ")
                    .append("'")
                    .append(dateFormat_date.format(option.getStartDate()))
                    .append("'")
                    .append(" and ")
                    .append("'")
                    .append(dateFormat_date.format(option.getEndDate()))
                    .append("'");
        }
        if ( option.getDeviceIds() != null) {
            for (String ipcid :  option.getDeviceIds()) {
                stringBuilder
                        .append(" and ")
                        .append(DynamicHiveTable.PARTITION_IPCID)
                        .append(" = ")
                        .append("'")
                        .append(ipcid)
                        .append("'");
            }
        }
        return stringBuilder.toString();
    }

}

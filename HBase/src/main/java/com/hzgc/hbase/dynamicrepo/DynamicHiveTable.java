package com.hzgc.hbase.dynamicrepo;

import java.io.Serializable;

/**
 * hive映射表属性
 */

public class DynamicHiveTable implements Serializable {

    /***
     * hive 数据目录 映射表的表名为person_table
     */
    public static final String PERSON_TABLE = "person_table";
    /***
     * hive 临时目录 映射表的表名为mid_table
     */
    public static final String MID_TABLE = "mid_table";
    /***
     * 图片url
     */
    public static final String PIC_URL = "ftpurl";
    /***
     * 特征值
     */
    public static final String FEATURE = "feature";
    /***
     * 人脸属性-眼镜
     */
    public static final String EYEGLASSES = "eyeglasses";
    /***
     * 人脸属性-性别
     */
    public static final String GENDER = "gender";
    /***
     * 人脸属性-头发颜色
     */
    public static final String HAIRCOLOR = "haircolor";
    /***
     * 人脸属性-头发类型
     */
    public static final String HAIRSTYLE = "hairstyle";
    /***
     * 人脸属性-帽子
     */
    public static final String HAT = "hat";
    /***
     * 人脸属性-胡子
     */
    public static final String HUZI = "huzi";
    /***
     * 人脸属性-领带
     */
    public static final String TIE = "tie";
    /***
     * 时间段
     */
    public static final String TIMESLOT = "timeslot";
    /***
     * 时间戳
     */
    public static final String TIMESTAMP = "timestamp";
    /***
     * 图片类型
     */
    public static final String PIC_TYPE = "pictype";
    /***
     * 设备id（分区条件）partition
     */
    public static final String PARTITION_IPCID = "ipcid";
    /***
     * date 数据格式 年-月-日（分区条件）
     */
    public static final String PARTITION_DATE = "date";
    /***
     * 相似度（临时字段）
     */
    public static final String SIMILARITY = "similarity";
    /***
     * hive udf 函数名
     */
    public static final String FUNCTION_NAME = "compare";
}

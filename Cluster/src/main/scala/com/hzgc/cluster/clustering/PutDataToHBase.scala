package com.hzgc.cluster.clustering

import java.util
import java.util.List

import com.hzgc.dubbo.clustering.{AlarmInfo, ClusteringAttribute}
import com.hzgc.service.clustering.ClusteringTable
import com.hzgc.service.util.HBaseHelper
import com.hzgc.util.common.ObjectUtil
import org.apache.hadoop.hbase.client.{Put, Table}
import org.apache.hadoop.hbase.util.Bytes

object PutDataToHBase {
  def main(args: Array[String]): Unit = {
    val clusteringAttributeList: util.List[ClusteringAttribute] = new util.ArrayList[ClusteringAttribute]
    val rowkey: String = "2018-05"
    val clusteringAttribute: ClusteringAttribute = new ClusteringAttribute()
    clusteringAttribute.setClusteringId("1")
    clusteringAttribute.setFirstAppearTime("2018-01-15")
    clusteringAttribute.setFtpUrl("ftp://s105/skdfjllj/fjdslafjl.jpg")
    clusteringAttributeList.add(clusteringAttribute)
    clusteringAttribute.setClusteringId("2")
    clusteringAttribute.setFirstAppearTime("2018-01-15")
    clusteringAttribute.setFtpUrl("ftp://s105/skdfjllj/fjdslafjl.jpg")
    clusteringAttribute.setClusteringId("3")
    clusteringAttribute.setFirstAppearTime("2018-01-15")
    clusteringAttribute.setFtpUrl("ftp://s105/skdfjllj/fjdslafjl.jpg")
    clusteringAttributeList.add(clusteringAttribute)
    clusteringAttribute.setCount(3)
    clusteringAttributeList.add(clusteringAttribute)
    putClusteringInfo(rowkey, clusteringAttributeList)
    val alarmInfo: AlarmInfo = new AlarmInfo()
    alarmInfo.setSurl("ftp://s100/20180102/xxx1.jpc")
    alarmInfo.setBurl("ftp://s100/20180102/xxx0.jpc")
    val alarmInfolist: util.List[AlarmInfo] = new util.ArrayList[AlarmInfo]
    putDetailInfo("20180101", alarmInfolist)
    print("hello")
  }

  /**
    * 保存聚类信息
    *
    * @param rowKey 年月（例如，2018-02）
    * @param list   聚类结果
    */
  def putClusteringInfo(rowKey: String, list: util.List[ClusteringAttribute]): Unit = {
    val ClusteringInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY, ClusteringTable.ClUSTERINGINFO_COLUMN_DATA, ObjectUtil.objectToByte(list))
    ClusteringInfo.put(put)
    ClusteringInfo.close()
  }

  /**
    *
    * @param rowKey 年月+类ID（例如，2018-02-1）
    * @param list   新增告警详细信息
    */
  def putDetailInfo(rowKey: String, list: util.List[AlarmInfo]): Unit = {
    val detailInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.DETAILINFO_COLUMNFAMILY, ClusteringTable.DETAILINFO_COLUMN_DATA, ObjectUtil.objectToByte(list))
    detailInfo.put(put)
    detailInfo.close()
  }

  /**
    *
    * @param rowKey 年月+类ID（例如，2018-02-1）
    * @param list   每条新增告警id
    */
  def putDetailInfo_v1(rowKey: String, list: List[Integer]): Unit = {
    val detailInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.DETAILINFO_COLUMNFAMILY, ClusteringTable.DETAILINFO_COLUMN_DATA, ObjectUtil.objectToByte(list))
    detailInfo.put(put)
    detailInfo.close()
  }
}

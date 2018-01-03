package com.hzgc.service.staticrepo;


import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ChangeFeatureByPkey {
    private static Logger LOG = Logger.getLogger(ChangeFeatureByPkey.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOG.error("Wrong number of input parameters");
        }
        String pKey = args[0];
        List<String[]> searchByKey = searchByKey(pKey);
        putData2HBase(searchByKey);
        putData2ES(searchByKey);
    }

    private static List<String[]> searchByKey(String pKey) {
        String feature = "";
        Table objectinfo = HBaseHelper.getTable("objectinfo");
        ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();

        //定义一个List用来存在查询得到的结果
        List<String[]> findResult = new ArrayList<>();
        //构造搜索对象
        SearchResponse searchResponse;
        //设置搜索条件
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        //根据遍历得到的人员类型进行精确查询
        requestBuilder.setQuery(QueryBuilders.termQuery(ObjectInfoTable.PKEY, pKey));
        //通过requestBuilder的get方法执行查询任务
        searchResponse = requestBuilder.get();
        //将结果进行封装
        SearchHits hits = searchResponse.getHits();
        //输出某个人员类型对应的记录条数
        SearchHit[] searchHits = hits.getHits();
        LOG.info("pkey为：" + pKey + "时，查询得到的记录数为：" + hits.getTotalHits());
        if (searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                //得到每个人员类型对应的rowkey
                String rowkey = hit.getId();
                Get get = new Get(Bytes.toBytes(rowkey));
                Result result = null;
                try {
                    result = objectinfo.get(get);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] photo = result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
                //得到每个人员类型对应的特征值
                feature = objectInfoHandler.getFeature("person", photo);
                //当有特征值时，才将结果返回
                if (!feature.equals("")) {
                    //将人员类型、rowkey和特征值进行拼接
                    String[] result1 = new String[3];
                    result1[0] = rowkey;
                    result1[1] = pKey;
                    result1[2] = feature;
                    //将结果添加到集合中
                    findResult.add(result1);
                }
            }
        }
        return findResult;
    }

    private static void putData2HBase(List<String[]> searchByKey) {
        Table objectinfo = HBaseHelper.getTable("objectinfo");
        Iterator<String[]> iterator = searchByKey.iterator();
        try {
            while (iterator.hasNext()) {
                String[] result = iterator.next();
                String rowkey = result[0];
                String feature = result[2];
                LOG.info("put data rowkey is:" + rowkey + "to HBase");
                Put put = new Put(Bytes.toBytes(rowkey));
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), feature.getBytes("UTF-8"));
                objectinfo.put(put);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtil.closTable(objectinfo);
        }

    }

    private static void putData2ES(List<String[]> searchByKey) {
        Iterator<String[]> iterator = searchByKey.iterator();
        Client client = ElasticSearchHelper.getEsClient();
        try {
            while (iterator.hasNext()) {
                String[] result = iterator.next();
                String rowkey = result[0];
                String feature = result[2];
                LOG.info("UPdata rowkey is:" + rowkey + "to ES");
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.index("objectinfo");
                updateRequest.type("person");
                updateRequest.id(rowkey);
                updateRequest.doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("feature", feature)
                        .endObject());
                UpdateResponse response = client.update(updateRequest).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}


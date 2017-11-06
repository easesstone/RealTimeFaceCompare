package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class CaptureHistory {
    private static Logger LOG = Logger.getLogger(CaptureHistory.class);

    static {
        ElasticSearchHelper.getEsClient();
    }


    SearchResult getRowKey_history(SearchOption option) {
        SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder_history(option);
        int count = option.getCount();
        return dealWithSearchRequestBuilder_history(searchRequestBuilder, count);
    }

    private SearchRequestBuilder getSearchRequestBuilder_history(SearchOption option) {
        // 传过来为空，返回空
        if (option == null) {
            return null;
        }
        // 获取搜索类型，搜索类型要么是人，要么是车，不可以为空，为空不处理
        SearchType searchType = option.getSearchType();
        // 搜索类型为空，则返回空。
        if (searchType == null) {
            return null;
        }

        // es 中的索引，
        String index = "";
        // es 中类型
        String type = "";
        // 最终封装成的boolQueryBuilder 对象。
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();

        int offset = option.getOffset();
        LOG.info("offset is:" + offset);
        int count = option.getCount();
        LOG.info("count is:" + count);

        // 搜索类型为人的情况下
        if (SearchType.PERSON.equals(searchType)) {
            // 获取设备ID
            List<String> deviceId = option.getDeviceIds();
            // 起始时间
            Date startTime = option.getStartDate();
            // 结束时间
            Date endTime = option.getEndDate();
            // 时间段
            List<TimeInterval> timeIntervals = option.getIntervals();
            //人脸属性--眼镜
            int eleglasses = option.getAttribute().getEyeglasses().getValue();
            //眼镜的状态值
            String elelog = String.valueOf(option.getAttribute().getEyeglasses().getLogistic());
            //人脸属性--性别
            int gender = option.getAttribute().getGender().getValue();
            //性别的状态值
            String genlog = String.valueOf(option.getAttribute().getGender().getLogistic());
            //人脸属性--头发颜色
            int haircolor = option.getAttribute().getHairColor().getValue();
            //头发颜色的状态值
            String collog = String.valueOf(option.getAttribute().getHairColor().getLogistic());
            //人脸属性--发型
            int hairstyle = option.getAttribute().getHairStyle().getValue();
            //发型的状态值
            String stylog = String.valueOf(option.getAttribute().getHairStyle().getLogistic());
            //人脸属性--帽子
            int hat = option.getAttribute().getHat().getValue();
            //帽子的状态值
            String hatlog = String.valueOf(option.getAttribute().getHat().getLogistic());
            //人脸属性--胡子
            int huzi = option.getAttribute().getHuzi().getValue();
            //胡子的状态值
            String huzlog = String.valueOf(option.getAttribute().getHuzi().getLogistic());
            //人脸属性--领带
            int tie = option.getAttribute().getTie().getValue();
            //领带的状态值
            String tielog = String.valueOf(option.getAttribute().getTie().getLogistic());
            // 设备ID 的的boolQueryBuilder
            BoolQueryBuilder devicdIdBQ = QueryBuilders.boolQuery();
            // 格式化时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 设备ID 存在的时候的处理
            if (deviceId != null) {
                for (Object t : deviceId) {
                    devicdIdBQ.should(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, t).analyzer("standard"));
                }
                totalBQ.must(devicdIdBQ);
            }
            //人脸属性筛选
            if (eleglasses != 0) {
                if (Objects.equals(elelog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.ELEGLASSES, eleglasses).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.ELEGLASSES, eleglasses).analyzer("standard"));
                }
            }
            if (gender != 0) {
                if (Objects.equals(genlog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.GENDER, gender).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.GENDER, gender).analyzer("standard"));
                }
            }
            if (haircolor != 0) {
                if (Objects.equals(collog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.HAIRCOLOR, haircolor).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.HAIRCOLOR, haircolor).analyzer("standard"));
                }
            }
            if (hairstyle != 0) {
                if (Objects.equals(stylog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.HAIRSTYLE, hairstyle).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.HAIRSTYLE, hairstyle).analyzer("standard"));
                }
            }
            if (hat != 0) {
                if (Objects.equals(hatlog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.HAT, hat).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.HAT, hat).analyzer("standard"));
                }
            }
            if (huzi != 0) {
                if (Objects.equals(huzlog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.HUZI, huzi).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.HUZI, huzi).analyzer("standard"));
                }
            }
            if (tie != 0) {
                if (Objects.equals(tielog, "AND")) {
                    totalBQ.must(QueryBuilders.matchQuery(DynamicTable.TIE, tie).analyzer("standard"));
                } else {
                    totalBQ.should(QueryBuilders.matchQuery(DynamicTable.TIE, tie).analyzer("standard"));
                }
            }
            // 开始时间和结束时间存在的时候的处理
            if (startTime != null && endTime != null) {
                String start = dateFormat.format(startTime);
                String end = dateFormat.format(endTime);
                totalBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(start).lte(end));
            }
            //TimeIntervals 时间段的封装类
            TimeInterval timeInterval;
            // 时间段的BoolQueryBuilder
            BoolQueryBuilder timeInQB = QueryBuilders.boolQuery();
            // 对时间段的处理
            if (timeIntervals != null) {
                for (TimeInterval timeInterval1 : timeIntervals) {
                    timeInterval = timeInterval1;
                    int start_sj = timeInterval.getStart();
                    String start_ts = String.valueOf(start_sj * 100 / 60 + start_sj % 60);
                    int end_sj = timeInterval.getEnd();
                    String end_ts = String.valueOf(end_sj * 100 / 60 + end_sj % 60);
                    timeInQB.should(QueryBuilders.rangeQuery(DynamicTable.TIMESLOT).gte(start_ts).lte(end_ts));
                    totalBQ.must(timeInQB);
                }
            }

            index = DynamicTable.DYNAMIC_INDEX;
            type = DynamicTable.PERSON_INDEX_TYPE;
        } else if (SearchType.CAR.equals(searchType)) {     // 搜索的是车的情况下

        }
        LOG.info("================================================");
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(index)
                .setTypes(type)
                .setFrom(offset)
                .setSize(100)
                .addSort(DynamicTable.TIMESTAMP, SortOrder.DESC);
        return requestBuilder.setQuery(totalBQ);
    }

    private SearchResult dealWithSearchRequestBuilder_history(SearchRequestBuilder searchRequestBuilder, int count) {
        // 最终要返回的值
        SearchResult result = new SearchResult();
        // requestBuilder 为空，则返回空
        if (searchRequestBuilder == null) {
            return result;
        }
        // 通过SearchRequestBuilder 获取response 对象。
        SearchResponse searchResponse = searchRequestBuilder.get();
        // 滚动查询
        SearchHits searchHits = searchResponse.getHits();
        result.setTotal((int) searchHits.getTotalHits());
        SearchHit[] hits = searchHits.getHits();
        List<CapturedPicture> persons = new ArrayList<>();
        CapturedPicture capturePicture;
        int i = 0;
        if (hits.length > 0) {
            for (SearchHit hit : hits) {
                capturePicture = new CapturedPicture();
                String rowKey = hit.getId();
                String ipcid = (String) hit.getSource().get(DynamicTable.IPCID);
                System.out.println(hit.getSourceAsString());
                String timestamp = (String) hit.getSource().get(DynamicTable.TIMESTAMP);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long time = 0;
                try {
                    Date date = sdf.parse(timestamp);
                    time = date.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String pictype = (String) hit.getSource().get(DynamicTable.PICTYPE);
                if (rowKey.endsWith("_00")) {
                    continue;
                }
                capturePicture.setId(rowKey);
                capturePicture.setIpcId(ipcid);
                capturePicture.setPictureType(PictureType.valueOf(pictype));
                capturePicture.setTimeStamp(time);
                persons.add(capturePicture);
                i++;
                if (i == count) {
                    break;
                }
            }
        }
        result.setPictures(persons);
        return result;
    }
}
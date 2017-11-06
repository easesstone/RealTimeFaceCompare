package com.hzgc.hbase.dynamicrepo;

import com.alibaba.dubbo.common.utils.LogUtil;
import com.hzgc.dubbo.Attribute.*;
import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.DateUtil;
import com.hzgc.util.ObjectUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * 以图搜图接口实现类，内含四个方法（外）（彭聪）
 */
public class CapturePictureSearchServiceImpl implements CapturePictureSearchService {
    private static Logger LOG = Logger.getLogger(CapturePictureSearchServiceImpl.class);
   /* private static SparkConf conf = new SparkConf().setAppName("RealTimeCompare").setMaster("local[*]");
    private static transient JavaSparkContext jsc = new JavaSparkContext(conf);*/

    static {
        ElasticSearchHelper.getEsClient();
        HBaseHelper.getHBaseConnection();
    }

    /**
     * 接收应用层传递的参数进行搜图（彭聪）
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    @Override
    public SearchResult search(SearchOption option) {
        RealTimeCompareBySparkSQL realTimeCompareBySparkSQL = new RealTimeCompareBySparkSQL();
        SearchResult searchResult = null;
        try {
            searchResult = realTimeCompareBySparkSQL.pictureSearchBySparkSQL(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 获取查询结果
     *
     * @param searchId 搜索的 id（rowkey）（刘思阳）
     * @param offset   从第几条开始
     * @param count    条数
     * @return SearchResult对象
     */
    @Override
    public SearchResult getSearchResult(String searchId, int offset, int count, String sortParams) {
        SearchResult searchResult = new SearchResult();
        if (null != searchId && !"".equals(searchId)) {
            List<CapturedPicture> capturedPictureList;
            Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
            Get get = new Get(Bytes.toBytes(searchId));
            searchResult.setSearchId(searchId);
            Result result = null;
            try {
                result = searchResTable.get(get);
                HBaseUtil.closTable(searchResTable);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.info("no result get by searchId[" + searchId + "]");
            }
            if (result != null) {
                String searchType = Bytes.toString(result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHTYPE));
                byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
                capturedPictureList = (List<CapturedPicture>) ObjectUtil.byteToObject(searchMessage);
                DynamicPhotoService dynamicPhotoService = new DynamicPhotoServiceImpl();
                if (searchType.equals(DynamicTable.PERSON_TYPE)) {
                    //结果集（capturedPictureList）分页返回
                    searchResult = sortAndSplit(capturedPictureList, offset, count);
                } else if (searchType.equals(DynamicTable.CAR_TYPE)) {
                    //结果集（capturedPictureList）分页返回
                    searchResult = sortAndSplit(capturedPictureList, offset, count);
                } else {
                    searchResult = sortAndSplit(capturedPictureList, offset, count);
                }
            } else {
                LOG.info("get searchMessageMap null from table_searchRes");
            }
        } else {
            LOG.info("searchId is null");
        }
        return searchResult;
    }

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 过滤参数键值对
     */
    @Override
    public Map<String, List<Integer>> getAttribute(SearchType type) {
        Map<String, List<Integer>> map = new LinkedHashMap<>();

        if (type == SearchType.PERSON) {
            List<Integer> hairColorList = new ArrayList<>();
            for (HairColor hc : HairColor.values()) {
                hairColorList.add(hc.ordinal());
            }
            map.put("HairColor", hairColorList);

            List<Integer> hairStyleList = new ArrayList<>();
            for (HairStyle hs : HairStyle.values()) {
                hairStyleList.add(hs.ordinal());
            }
            map.put("HairStyle", hairStyleList);

            List<Integer> genderList = new ArrayList<>();
            for (Gender gender : Gender.values()) {
                genderList.add(gender.ordinal());
            }
            map.put("Gender", genderList);

            List<Integer> hatList = new ArrayList<>();
            for (Hat hat : Hat.values()) {
                hatList.add(hat.ordinal());
            }
            map.put("Hat", hatList);

            List<Integer> tieList = new ArrayList<>();
            for (Tie tie : Tie.values()) {
                tieList.add(tie.ordinal());
            }
            map.put("Tie", tieList);

            List<Integer> huziList = new ArrayList<>();
            for (Huzi huzi : Huzi.values()) {
                huziList.add(huzi.ordinal());
            }
            map.put("Huzi", huziList);

            List<Integer> eyeglassesList = new ArrayList<>();
            for (Eyeglasses eyeglasses : Eyeglasses.values()) {
                eyeglassesList.add(eyeglasses.ordinal());
            }
            map.put("Eyeglasses", eyeglassesList);

        } else if (type == SearchType.CAR) {

        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getAttribute SearchType is error.");
        }
        return map;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowkey）
     * @param type    图片类型，人/车
     * @return DynamicObject    动态库对象
     */
    @Override
    public CapturedPicture getCaptureMessage(String imageId, int type) {
        CapturedPicture capturedPicture = new CapturedPicture();
        if (null != imageId) {
            capturedPicture.setId(imageId);
            String rowKey = imageId.substring(0, imageId.lastIndexOf("_"));
            StringBuilder bigImageRowKey = new StringBuilder();
            bigImageRowKey.append(rowKey).append("_").append("00");

            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            Map<String, Object> mapEx = new HashMap<>();
            switch (type) {
                case 0:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);
                        setSmallImageToCapturedPicture_person(capturedPicture, result);
                        setCapturedPicture_person(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = person.get(bigImageGet);
                        setBigImageToCapturedPicture_person(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 0");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 1:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);
                        setSmallImageToCapturedPicture_car(capturedPicture, result);
                        setCapturedPicture_car(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = car.get(bigImageGet);
                        setBigImageToCapturedPicture_car(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 1");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 2:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);

                        setSmallImageToCapturedPicture_person(capturedPicture, result);
                        setCapturedPicture_person(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 2");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 3:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);

                        setSmallImageToCapturedPicture_car(capturedPicture, result);
                        setCapturedPicture_car(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 3");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 4:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);
                        setCapturedPicture_person(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = person.get(bigImageGet);
                        setBigImageToCapturedPicture_person(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 4");
                    } finally {
                        HBaseUtil.closTable(person);
                    }
                    break;
                case 5:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);
                        setCapturedPicture_car(capturedPicture, result, mapEx);

                        Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                        Result bigImageResult = car.get(bigImageGet);
                        setBigImageToCapturedPicture_car(capturedPicture, bigImageResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 5");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 6:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = person.get(get);
                        setCapturedPicture_person(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 6");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                case 7:
                    try {
                        Get get = new Get(Bytes.toBytes(imageId));
                        Result result = car.get(get);
                        setCapturedPicture_car(capturedPicture, result, mapEx);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 7");
                    } finally {
                        HBaseUtil.closTable(car);
                    }
                    break;
                default:
                    LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage param is error.");
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage imageId is empty.");
        }
        return capturedPicture;
    }

    /**
     * 批量查询图片对象（彭聪）
     *
     * @param imageIdList 图片ID列表
     * @param type        搜索类型
     * @return List<CapturedPicture> 图片对象列表
     */
    @Override
    public List<CapturedPicture> getBatchCaptureMessage(List<String> imageIdList, int type) {
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        if (imageIdList != null) {
            List<Get> gets = new ArrayList<>();
            CapturedPicture capturedPicture;
            if (type == PictureType.SMALL_PERSON.getType()) {
                Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
                for (String anImageIdList : imageIdList) {
                    Get get = new Get(Bytes.toBytes(anImageIdList));
                    get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    gets.add(get);
                }
                Result[] results = new Result[imageIdList.size()];
                try {
                    results = person.get(gets);
                    HBaseUtil.closTable(person);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (results != null) {
                    for (Result result : results) {
                        capturedPicture = new CapturedPicture();
                        if (result != null) {
                            String rowKey = Bytes.toString(result.getRow());
                            capturedPicture.setId(rowKey);
                            byte[] imageData = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                            capturedPicture.setSmallImage(imageData);
                            Map<String, Object> mapEx = new HashMap<>();
                            setCapturedPicture_person(capturedPicture, result, mapEx);
                            capturedPictureList.add(capturedPicture);
                        } else {
                            LOG.error("get Result from table_person is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                        }
                    }
                } else {
                    LOG.error("get Result[] from table_person is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                }
            } else if (type == PictureType.SMALL_CAR.getType()) {
                Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                for (String anImageIdList : imageIdList) {
                    Get get = new Get(Bytes.toBytes(anImageIdList));
                    get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
                    gets.add(get);
                }
                Result[] results = new Result[imageIdList.size()];
                try {
                    results = car.get(gets);
                    HBaseUtil.closTable(car);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (results != null) {
                    for (Result result : results) {
                        capturedPicture = new CapturedPicture();
                        if (result != null) {
                            String rowKey = Bytes.toString(result.getRow());
                            byte[] imageData = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
                            capturedPicture.setId(rowKey);
                            capturedPicture.setSmallImage(imageData);
                            Map<String, Object> mapEx = new HashMap<>();
                            setCapturedPicture_car(capturedPicture, result, mapEx);
                            capturedPictureList.add(capturedPicture);
                        } else {
                            LOG.error("get Result from table_car is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                        }
                    }
                } else {
                    LOG.error("get Result[] from table_car is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                }

            } else {
                LOG.info("the type is error");
            }
        }
        return capturedPictureList;
    }

    private void setSmallImageToCapturedPicture_person(CapturedPicture capturedPicture, Result result) {
        if (result != null) {
            byte[] smallImage = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
            if (smallImage != null) {
                capturedPicture.setSmallImage(smallImage);
            } else {
                LOG.warn("get smallImage is null!");
            }
        } else {
            LOG.error("get Result from table_person is null! used method CapturePictureSearchServiceImpl.setSmallImageToCapturedPicture_person.");
        }
    }

    private void setCapturedPicture_person(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        if (result != null) {
            String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
            capturedPicture.setDescription(des);

            String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
            mapEx.put("ex", ex);
            capturedPicture.setExtend(mapEx);

            String ipcId = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID));
            capturedPicture.setIpcId(ipcId);

            String time = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP));
            long timeStamp = DateUtil.dateToTimeStamp(time);
            capturedPicture.setTimeStamp(timeStamp);
        } else {
            LOG.error("get Result from table_person is null! used method CapturePictureSearchServiceImpl.setCapturedPicture_person.");
        }
    }

    private void setBigImageToCapturedPicture_person(CapturedPicture capturedPicture, Result bigImageResult) {
        if (bigImageResult != null) {
            byte[] bigImage = bigImageResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
            if (bigImage != null) {
                capturedPicture.setBigImage(bigImage);
            } else {
                LOG.warn("get bigImage is null!");
            }
        } else {
            LOG.error("get Result from table_person is null! used method CapturePictureSearchServiceImpl.setBigImageToCapturedPicture_person.");
        }
    }

    private void setSmallImageToCapturedPicture_car(CapturedPicture capturedPicture, Result result) {
        if (result != null) {
            byte[] smallImage = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
            if (smallImage != null) {
                capturedPicture.setSmallImage(smallImage);
            } else {
                LOG.warn("get smallImage is null!");
            }
        } else {
            LOG.error("get Result from table_car is null! used method CapturePictureSearchServiceImpl.setSmallImageToCapturedPicture_car.");
        }
    }

    private void setCapturedPicture_car(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        if (result != null) {
            String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
            capturedPicture.setDescription(des);

            String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
            mapEx.put("ex", ex);
            capturedPicture.setExtend(mapEx);

            String ipcId = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IPCID));
            capturedPicture.setIpcId(ipcId);

            String time = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_TIMESTAMP));
            long timeStamp = DateUtil.dateToTimeStamp(time);
            capturedPicture.setTimeStamp(timeStamp);

            String plateNumber = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM));
            capturedPicture.setPlateNumber(plateNumber);
        } else {
            LOG.error("get Result from table_car is null! used method CapturePictureSearchServiceImpl.setCapturedPicture_car.");
        }
    }

    private void setBigImageToCapturedPicture_car(CapturedPicture capturedPicture, Result bigImageResult) {
        if (bigImageResult != null) {
            byte[] bigImage = bigImageResult.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
            if (bigImage != null) {
                capturedPicture.setBigImage(bigImage);
            } else {
                LOG.warn("get bigImage is null!");
            }
        } else {
            LOG.error("get Result from table_car is null! used method CapturePictureSearchServiceImpl.setBigImageToCapturedPicture_car.");
        }
    }

    /**
     * 排序分页 （彭聪）
     *
     * @param capturedPictureList 图片对象列表
     * @param offset              起始值
     * @param count               总条数
     * @return 返回分页排序后的结果
     */
    private SearchResult sortAndSplit(List<CapturedPicture> capturedPictureList, int offset, int count) {
        //SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        SearchResult tempResult = new SearchResult();
        if (null != capturedPictureList && capturedPictureList.size() > 0) {
            ///保存记录时已经排好序，分页返回时不需要排序，如果需要对下一页采用新的排序方式可以解除注释第一行和下面一行代码
            //ListUtils.sort(capturedPictureList, sortParam.getSortNameArr(), sortParam.getIsAscArr());
            List<CapturedPicture> subCapturePictureList;
            if (offset > -1 && capturedPictureList.size() > (offset + count - 1)) {
                //结束行小于总数
                subCapturePictureList = capturedPictureList.subList(offset, offset + count);
            } else {
                //结束行大于总数
                subCapturePictureList = capturedPictureList.subList(offset, capturedPictureList.size());
            }
            tempResult.setPictures(subCapturePictureList);
            tempResult.setTotal(capturedPictureList.size());
            return tempResult;
        } else {
            LOG.error("capturedPictureList is null");
        }
        return tempResult;
    }


    /**
     * 抓拍统计查询接口（马燊偲）
     * 查询指定时间段内，指定设备抓拍的图片数量、该设备最后一次抓拍时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipcId     设备ID
     * @return CaptureCount 查询结果对象。对象内封装了：该时间段内该设备抓拍张数，该时间段内该设备最后一次抓拍时间。
     */
    public CaptureCount captureCountQuery(String startTime, String endTime, String ipcId) {

        //CaptureCount是一个封装类，用于封装返回的结果。
        CaptureCount returnresult = new CaptureCount();
        if (null != startTime && startTime.length() > 0
                && null != endTime && endTime.length() > 0
                && null != ipcId && ipcId.length() > 0) {

            //设定查询条件：指定时间段startTime至endTime，指定设备ipcId
            QueryBuilder qb = boolQuery()
                    .must(matchQuery("s", ipcId))
                    .must(rangeQuery("t").gte(startTime).lte(endTime));//gte: >= 大于或等于；lte: <= 小于或等于

            SearchResponse searchResponse = ElasticSearchHelper.getEsClient() //启动Es Java客户端
                    .prepareSearch(DynamicTable.DYNAMIC_INDEX) //指定要查询的索引名称
                    .setTypes(DynamicTable.PERSON_INDEX_TYPE) //指定要查询的类型名称
                    .setQuery(qb) //根据查询条件qb设置查询
                    .addSort("t", SortOrder.DESC) //以时间字段降序排序
                    .get();

            SearchHits hits = searchResponse.getHits(); //返回结果包含的文档放在数组hits中
            long totalresultcount = hits.getTotalHits(); //符合qb条件的结果数量

            //返回结果包含的文档放在数组hits中
            SearchHit[] searchHits = hits.hits();
            //若不存在符合条件的查询结果
            if (totalresultcount == 0) {
                LOG.error("The result count is 0! Last capture time does not exist!");
                returnresult.setTotalresultcount(totalresultcount);
                returnresult.setLastcapturetime("None");
            } else {
                /**
                 * 获取该时间段内设备最后一次抓拍时间：
                 * 返回结果包含的文档放在数组hits中，由于结果按照降序排列，
                 * 因此hits数组里的第一个值代表了该设备最后一次抓拍的具体信息
                 * 例如{"s":"XXXX","t":"2017-09-20 15:55:06","sj":"1555"}
                 * 将该信息以Map形式读取，再获取到key="t“的值，即最后一次抓拍时间。
                 **/

                //获取最后一次抓拍时间
                String lastcapturetime = (String) searchHits[0].getSourceAsMap().get("t");

                /**
                 * 返回值为：设备抓拍张数、设备最后一次抓拍时间。
                 **/
                returnresult.setTotalresultcount(totalresultcount);
                returnresult.setLastcapturetime(lastcapturetime);
            }
        } else {
            LOG.error("The Input parameters are wrong!");
        }
        return returnresult;
    }

    @Override
    public SearchResult getCaptureHistory(SearchOption option) {
        option.setSearchType(SearchType.PERSON);
        long esStartTime = System.currentTimeMillis();
        SearchResult searchResult = getImageIdListFromEs_History(option);
        long esEndTime = System.currentTimeMillis();
        LOG.info("search" + searchResult.getTotal() + " history image from es takes:" + (esEndTime - esStartTime) + "ms");
        return searchResult;
    }

    private SearchResult getImageIdListFromEs_History(SearchOption option) {
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        return filterByRowkey.getRowKey_history(option);
    }

    /**
     * 抓拍属性统计查询 (刘思阳)
     * 查询指定时间段内，单个或某组设备中某种属性在抓拍图片中的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipcIdList 单个或某组设备ID
     * @return 单个或某组设备中某种属性在抓拍图片中的数量（Map<设备ID, AttributeCount>）
     */
    @Override
    public Map<String, AttributeCount> captureAttributeQuery(String startTime, String endTime, List<String> ipcIdList, SearchType type) {
        Map<String, AttributeCount> map = new LinkedHashMap<>();
        if (type == SearchType.PERSON) {
            CapturePictureSearchService service = new CapturePictureSearchServiceImpl();
            if (ipcIdList != null && ipcIdList.size() > 0) {
                for (String ipcId : ipcIdList) {
                    AttributeCount attributeCount = new AttributeCount();

                    //头发颜色属性统计（Map<属性, 数量>）
                    Map<Integer, Long> hairColorMap = new LinkedHashMap<>();
                    for (HairColor hc : HairColor.values()) {
                        Integer ordinal = hc.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.HAIRCOLOR, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        hairColorMap.put(ordinal, totalHits);
                    }
                    attributeCount.setHairColorMap(hairColorMap);

                    //头发类型属性统计（Map<属性, 数量>）
                    Map<Integer, Long> hairStyleMap = new LinkedHashMap<>();
                    for (HairStyle hs : HairStyle.values()) {
                        Integer ordinal = hs.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.HAIRSTYLE, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        hairStyleMap.put(ordinal, totalHits);
                    }
                    attributeCount.setHairStyleMap(hairStyleMap);

                    //性别属性统计（Map<属性, 数量>）
                    Map<Integer, Long> genderMap = new LinkedHashMap<>();
                    for (Gender gender : Gender.values()) {
                        Integer ordinal = gender.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.GENDER, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        genderMap.put(ordinal, totalHits);
                    }
                    attributeCount.setGenderMap(genderMap);

                    //是否带帽子属性统计（Map<属性, 数量>）
                    Map<Integer, Long> hatMap = new LinkedHashMap<>();
                    for (Hat hat : Hat.values()) {
                        Integer ordinal = hat.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.HAT, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        hatMap.put(ordinal, totalHits);
                    }
                    attributeCount.setHatMap(hatMap);

                    //是否系领带属性统计（Map<属性, 数量>）
                    Map<Integer, Long> tieMap = new LinkedHashMap<>();
                    for (Tie tie : Tie.values()) {
                        Integer ordinal = tie.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.TIE, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        tieMap.put(ordinal, totalHits);
                    }
                    attributeCount.setTieMap(tieMap);

                    //胡子类型属性统计（Map<属性, 数量>）
                    Map<Integer, Long> huziMap = new LinkedHashMap<>();
                    for (Huzi huzi : Huzi.values()) {
                        Integer ordinal = huzi.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.HUZI, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        huziMap.put(ordinal, totalHits);
                    }
                    attributeCount.setHuziMap(huziMap);

                    //是否戴眼镜属性统计（Map<属性, 数量>）
                    Map<Integer, Long> eyeglassesMap = new LinkedHashMap<>();
                    for (Eyeglasses eyeglasses : Eyeglasses.values()) {
                        Integer ordinal = eyeglasses.ordinal();

                        BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.IPCID, ipcId));
                        FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                        FilterIpcId.must(QueryBuilders.matchQuery(DynamicTable.ELEGLASSES, ordinal));
                        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                .setQuery(FilterIpcId).get();
                        SearchHits hits = searchResponse.getHits();
                        long totalHits = hits.getTotalHits();
                        eyeglassesMap.put(ordinal, totalHits);
                    }
                    attributeCount.setEyeglassesMap(eyeglassesMap);

                    CaptureCount captureCount = service.captureCountQuery(startTime, endTime, ipcId);
                    long count = captureCount.getTotalresultcount();
                    attributeCount.setCaptureCount(count);

                    map.put(ipcId, attributeCount);
                }
            } else {
                LOG.error("ipcIdList is null.");
            }
        } else if (type == SearchType.CAR) {

        } else {
            LOG.error("method CapturePictureSearchServiceImpl.captureAttributeQuery SearchType is error.");
        }

        return map;
    }
}

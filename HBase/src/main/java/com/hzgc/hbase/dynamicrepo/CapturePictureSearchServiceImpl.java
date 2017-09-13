package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.DateUtil;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.ObjectUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 接收应用层传递的参数进行搜图
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    @Override
    public SearchResult search(SearchOption option) {
        RealTimeCompare realTimeCompare = new RealTimeCompare();
        SearchResult searchResult = null;
        try {
            searchResult = realTimeCompare.pictureSearch(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * @param searchId 搜索的 id（rowkey）（刘思阳）
     * @param offset   从第几条开始
     * @param count    条数
     * @return SearchResult对象
     */
    @Override
    public SearchResult getSearchResult(String searchId, int offset, int count, String sortParams) {
        SearchResult searchResult = new SearchResult();
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Get get = new Get(Bytes.toBytes(searchId));
        try {
            Result result = searchResTable.get(get);
            if (result != null) {
                byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
                capturedPictureList = (List<CapturedPicture>) ObjectUtil.byteToObject(searchMessage);
                //排序分页
                searchResult = sortAndSplit(capturedPictureList, offset, count, sortParams);
                if (searchResult != null) {
                    searchResult.setSearchId(searchId);
                }
            } else {
                LOG.error("get Result form table_searchRes is null! used method CapturePictureSearchServiceImpl.getSearchResult.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("get data by searchId from table_searchRes failed! used method CapturePictureSearchServiceImpl.getSearchResult.");
        } finally {
            HBaseUtil.closTable(searchResTable);
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
    public Map<String, String> getSearchFilterParams(int type) {
        return null;
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
     * 批量查询图片对象
     *
     * @param imageIdList 图片ID列表
     * @param type        搜索类型
     * @return List<CapturedPicture> 图片对象列表
     */
    @Override
    public List<CapturedPicture> getBatchCaptureMessage(List<String> imageIdList, int type) {
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        if (imageIdList != null) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            List<Get> gets = new ArrayList<>();
            CapturedPicture capturedPicture;
            try {
                if (type == PictureType.PERSON.getType()) {
                    for (int i = 0, len = imageIdList.size(); i < len; i++) {
                        Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                        get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                        gets.add(get);
                    }
                    Result[] results = person.get(gets);
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
                                LOG.error("get Result form table_person is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                            }
                        }
                    } else {
                        LOG.error("get Result[] form table_person is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                    }
                } else if (type == PictureType.CAR.getType()) {
                    for (int i = 0, len = imageIdList.size(); i < len; i++) {
                        Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                        get.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
                        gets.add(get);
                    }
                    Result[] results = car.get(gets);
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
                                LOG.error("get Result form table_car is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                            }
                        }
                    } else {
                        LOG.error("get Result[] form table_car is null! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("get List<CapturedPicture> by List<rowKey> from table_person or table_car failed! used method CapturePictureSearchServiceImpl.getBatchCaptureMessage.");
            } finally {
                HBaseUtil.closTable(person);
                HBaseUtil.closTable(car);
            }
        }
        return capturedPictureList;
    }

    /**
     * 查询某个摄像头照片历史所有图片，包括人和车
     *
     * @param searchId   搜索的 id（rowkey）
     * @param offset     从第几条开始
     * @param count      条数
     * @param sortParams 排序参数
     * @return SearchResult对象
     */
    @Override
    public SearchResult getCaptureHistory(String searchId, int offset, int count, String sortParams) {
        SearchResult searchResult = new SearchResult();
        if (null != searchId && !"".equals(searchId)) {
            List<CapturedPicture> capturedPictureList = new ArrayList<>();
            Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
            Get get = new Get(Bytes.toBytes(searchId));
            Result result = null;
            try {
                result = searchResTable.get(get);
                HBaseUtil.closTable(searchResTable);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.info("no result get by searchId[" + searchId + "]");
            }
            if (result != null) {
                byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
                capturedPictureList = (List<CapturedPicture>) ObjectUtil.byteToObject(searchMessage);
                //结果集（capturedPictureList）排序
                searchResult = sortAndSplit(capturedPictureList, offset, count, sortParams);
                if (searchResult != null) {
                    searchResult.setSearchId(searchId);
                } else {
                    LOG.info("searchResult is null get by method DynamicPhotoServiceImpl.sortAndSplit()");
                }
            } else {
                LOG.info("get searchMessageMap null from table_searchRes");
            }
        } else {
            LOG.info("searchId is null");
        }
        return searchResult;
    }

    private void setSmallImageToCapturedPicture_person(CapturedPicture capturedPicture, Result result) {
        if (result != null) {
            byte[] smallImage = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
            capturedPicture.setSmallImage(smallImage);
        } else {
            LOG.error("get Result form table_person is null! used method CapturePictureSearchServiceImpl.setSmallImageToCapturedPicture_person.");
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
            LOG.error("get Result form table_person is null! used method CapturePictureSearchServiceImpl.setCapturedPicture_person.");
        }
    }

    private void setBigImageToCapturedPicture_person(CapturedPicture capturedPicture, Result bigImageResult) {
        if (bigImageResult != null) {
            byte[] bigImage = bigImageResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
            capturedPicture.setBigImage(bigImage);
        } else {
            LOG.error("get Result form table_person is null! used method CapturePictureSearchServiceImpl.setBigImageToCapturedPicture_person.");
        }
    }

    private void setSmallImageToCapturedPicture_car(CapturedPicture capturedPicture, Result result) {
        if (result != null) {
            byte[] smallImage = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
            capturedPicture.setSmallImage(smallImage);
        } else {
            LOG.error("get Result form table_car is null! used method CapturePictureSearchServiceImpl.setSmallImageToCapturedPicture_car.");
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
            LOG.error("get Result form table_car is null! used method CapturePictureSearchServiceImpl.setCapturedPicture_car.");
        }
    }

    private void setBigImageToCapturedPicture_car(CapturedPicture capturedPicture, Result bigImageResult) {
        if (bigImageResult != null) {
            byte[] bigImage = bigImageResult.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
            capturedPicture.setBigImage(bigImage);
        } else {
            LOG.error("get Result form table_car is null! used method CapturePictureSearchServiceImpl.setBigImageToCapturedPicture_car.");
        }
    }

    private SearchResult sortAndSplit(List<CapturedPicture> capturedPictureList, int offset, int count, String sortParams) {
        //结果集（capturedPictureList）排序
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        SearchResult tempResult = new SearchResult();
        if (null != capturedPictureList && capturedPictureList.size() > 0) {
            ListUtils.sort(capturedPictureList, sortParam.getSortNameArr(), sortParam.getIsAscArr());
            //排序后的结果集分页
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
}

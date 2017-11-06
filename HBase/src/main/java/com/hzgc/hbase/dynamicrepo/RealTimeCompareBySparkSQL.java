package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.util.FtpImageUtil;
import com.hzgc.hbase.util.JDBCUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.FileUtil;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * 通过parkSQL以图搜图
 */
public class RealTimeCompareBySparkSQL {

    private Logger LOG = Logger.getLogger(RealTimeCompareBySparkSQL.class);
    /***
     * 获取jdbc连接
     */
    private JDBCUtil jdbcUtil = JDBCUtil.getInstance();
    /**
     * 图片的二进制数据
     */
    private byte[] image;
    /**
     * 图片 id ,优先使用图片流数组
     */
    private String imageId;
    /**
     * 排序参数
     */
    private String sortParams;
    /**
     * 分页查询开始行
     */
    private int offset;
    /**
     * 分页查询条数
     */
    private int count;
    /**
     * 查询Id 由UUID生成
     */
    private String searchId;
    /**
     * 查询结果，最终的返回值
     */
    private static SearchResult searchResult = new SearchResult();
    /**
     * 搜索类型
     */
    private SearchType searchType;

    /**
     * 图片对象列表
     */
    private List<CapturedPicture> capturedPictureList;
    /**
     * 图片对象
     */
    private CapturedPicture capturedPicture;
    private String insertType;
    private Properties propertie = new Properties();
    private DynamicPhotoService dynamicPhotoService;
    private CapturePictureSearchServiceImpl capturePictureSearchService;

     protected RealTimeCompareBySparkSQL() {

        dynamicPhotoService = new DynamicPhotoServiceImpl();
         capturePictureSearchService = new CapturePictureSearchServiceImpl();
        //获取ftp配置文件,并初始化propertie
        try {
            File resourceFile = FileUtil.loadResourceFile("ftp.properties");
            if (resourceFile != null) {
                propertie.load(new FileInputStream(resourceFile));
            }
        } catch (Exception e) {
            LOG.info("get ftp.properties failure");
        }

    }

    protected SearchResult pictureSearchBySparkSQL(SearchOption option) {
        if (null != option) {
            //搜索类型 是人还是车
            searchType = option.getSearchType();
            //排序参数
            sortParams = option.getSortParams();
            //上传图片数据
            image = option.getImage();
            imageId = option.getImageId();
            //分页查询开始行
            offset = option.getOffset();
            //分页查询数
            count = option.getCount();
            //设置查询Id
            searchId = UuidUtil.setUuid();
            if (null != searchType) {
                //查询的对象库是人
                if (searchType == SearchType.PERSON) {
                    insertType = DynamicTable.PERSON_TYPE;
                    PictureType pictureType = PictureType.SMALL_PERSON;
                    if (null != image && image.length > 0) {
                        searchResult = compareByImageBySparkSQL(pictureType, option);
                    } else {
                        //无图，有imageId
                        if (null != imageId) {
                            searchResult = compareByImageIdBySparkSQL(option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            capturePictureSearchService.getCaptureHistory(option);
                        }
                    }
                }
                //查询的对象库是车
                else if (searchType == SearchType.CAR) {
                    insertType = DynamicTable.CAR_TYPE;
                    PictureType pictureType = PictureType.SMALL_CAR;
                    //平台上传的参数中有图片
                    if (null != image && image.length > 0) {
                        searchResult = compareByImageBySparkSQL(pictureType, option);
                    } else {
                        //没有图片，有imageId,相当于ftpurl
                        if (null != imageId) {
                            searchResult = compareByImageIdBySparkSQL(option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            capturePictureSearchService.getCaptureHistory(option);
                        }
                    }
                }
            }
        } else {
            LOG.error("search parameter option is null");
            searchResult.setSearchId(null);
            searchResult.setPictures(null);
            searchResult.setTotal(0);
        }
        return searchResult;
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param pictureType 图片类型（人、车）SearchOption 过滤条件
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImageBySparkSQL(PictureType pictureType, SearchOption option) {
        //提取上传图片的特征值
        float[] searchFea = FaceFunction.featureExtract(image).getFeature();
        //将图片特征插入到特征库
        boolean insertStatus = dynamicPhotoService.upPictureInsert(pictureType, searchId, searchFea, image);
        if (insertStatus) {
            LOG.info("feature[" + searchId + "]insert into HBase successful");
        } else {
            LOG.error("feature[" + searchId + "] insert into HBase failed");
        }
        //判断特征值是否符合
        if (null != searchFea && searchFea.length == 512) {
            //将float[]特征值转为String特征值
            String searchFeaStr = FaceFunction.floatArray2string(searchFea);
            //特征值比对，根据条件过滤
            String selectBySparkSQL = getSQLwithOption(searchFeaStr, option);
            jdbcUtil.executeQuery(selectBySparkSQL, null, new JDBCUtil.QueryCallback() {
                @Override
                public void process(ResultSet rs) throws Exception {
                    while (rs.next()) {
                        //图片ftpurl
                        String imageid = rs.getString(DynamicHiveTable.PIC_URL);
                        //设备id
                        String ipcid = rs.getString(DynamicHiveTable.PARTITION_IPCID);
                        //相似度
                        Float similaritys = rs.getFloat(DynamicHiveTable.SIMILARITY);
                        //时间戳
                        Long timestamp = rs.getLong(DynamicHiveTable.TIMESTAMP);
                        //图片类型
                        String pic_type = rs.getString(DynamicHiveTable.PIC_TYPE);
                        capturedPicture = new CapturedPicture();
                        capturedPicture.setId(imageid);
                        capturedPicture.setIpcId(ipcid);
                        capturedPicture.setTimeStamp(timestamp);
                        capturedPicture.setSimilarity(similaritys);
                        capturedPicture.setPictureType(PictureType.valueOf(pic_type));
                    }
                }
            });
            capturedPictureList = new ArrayList<>();
            capturedPictureList.add(capturedPicture);
            searchResult = sortAndSplit(capturedPictureList, sortParams, offset, count);
        } else {
            LOG.info("search feature is null or short than 512");
        }
        return searchResult;
    }

    /**
     * 以图搜图，图片为空,通过图片id的查询方法
     *
     * @param option 过滤条件
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImageIdBySparkSQL(SearchOption option) {

        //通过imageId，到ftp找到对应图片的二进制数据
        byte[] image = FtpImageUtil.downloadftpFile2Bytes(
                propertie.getProperty("ftpuser"),
                propertie.getProperty("ftppassword"),
                imageId);
        if (image != null && image.length > 0) {
        //提取上传图片的特征值
        float[] searchFea = FaceFunction.featureExtract(image).getFeature();
        if (null != searchFea && searchFea.length == 512) {
            //将float[]特征值转为String特征值
            String searchFeaStr = FaceFunction.floatArray2string(searchFea);
            String selectBySparkSQL = getSQLwithOption(searchFeaStr, option);
            jdbcUtil.executeQuery(selectBySparkSQL, null, new JDBCUtil.QueryCallback() {
                @Override
                public void process(ResultSet rs) throws Exception {
                    //图片ftpurl
                    String imageid = rs.getString(DynamicHiveTable.PIC_URL);
                    //设备id
                    String ipcid = rs.getString(DynamicHiveTable.PARTITION_IPCID);
                    //相似度
                    Float similaritys = rs.getFloat(DynamicHiveTable.SIMILARITY);
                    //时间戳
                    Long timestamp = rs.getLong(DynamicHiveTable.TIMESTAMP);
                    //图片类型
                    String pic_type = rs.getString(DynamicHiveTable.PIC_TYPE);

                    capturedPicture = new CapturedPicture();
                    capturedPicture.setId(imageid);
                    capturedPicture.setIpcId(ipcid);
                    capturedPicture.setTimeStamp(timestamp);
                    capturedPicture.setSimilarity(similaritys);
                    capturedPicture.setPictureType(PictureType.valueOf(pic_type));
                }
            });
            capturedPictureList = new ArrayList<>();
            capturedPictureList.add(capturedPicture);
            searchResult = sortAndSplit(capturedPictureList, sortParams, offset, count);
        } else {
            LOG.info("search feature is null or short than 512");
        }
    }else {
            LOG.info("search image is null with ["+imageId+"] ");
        }
            return searchResult;
    }

    /***
     * 获取根据过滤条件拼接成的sql
     *
     * @param searchFeaStr 通过图片获取的特征值
     * @param option    过滤条件
     * @return 拼接的sql
     */
    private String getSQLwithOption(String searchFeaStr, SearchOption option) {
        FilterByOption filterByOption = new FilterByOption();
        return filterByOption.getSQLwithOption(searchFeaStr, option);
    }

    /**
     * 根据阈值过滤后的imageIdList批量查询数据对象分组排序
     *
     * @param capturedPictures 根据阈值过滤之后的对象列表
     * @return 最终查询结果
     */
    private SearchResult sortAndSplit(List<CapturedPicture> capturedPictures, String sortParams, int offset, int count) {
        SearchResult searchResultTemp = new SearchResult();
        List<CapturedPicture> capturedPicturesSorted;
        if (null != capturedPictures && capturedPictures.size() > 0) {
            capturedPicturesSorted = sortByParams(capturedPictures, sortParams);
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, capturedPicturesSorted, insertType);
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPicturesSorted, offset, count);
            searchResultTemp = new SearchResult();
            searchResultTemp.setPictures(subCapturedPictures);
            searchResultTemp.setSearchId(searchId);
            searchResultTemp.setTotal(capturedPictures.size());
        } else {
            LOG.info("Find no image by deviceIds or timeStamp");
        }
        return searchResultTemp;
    }

    /**
     * 根据排序参数对图片对象列表进行排序，支持多字段
     *
     * @param capturedPictures 待排序的图片对象列表
     * @param sortParams       排序参数
     * @return 排序后的图片对象列表
     */
    private List<CapturedPicture> sortByParams(List<CapturedPicture> capturedPictures, String sortParams) {
        //对排序参数进行读取和预处理
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        if (null != sortParams && sortParams.length() > 0) {
            ListUtils.sort(capturedPictures, sortParam.getSortNameArr(), sortParam.getIsAscArr());
        } else {
            LOG.info("sortParams is null!");
        }
        return capturedPictures;
    }

    /**
     * 对图片对象列表进行分页返回
     *
     * @param capturedPictures 待分页的图片对象列表
     * @param offset           起始行
     * @param count            条数
     * @return 返回分页查询结果
     */
    private List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset > -1 && totalPicture > (offset + count - 1)) {

            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }

    public static void main(String[] args) {

        RealTimeCompareBySparkSQL realTimeCompareBySparkSQLTwo = new RealTimeCompareBySparkSQL();

        SearchOption searchOption = new SearchOption();
        searchOption.setThreshold(60.00F);
        List<String> ipcId = new ArrayList<>();
        ipcId.add("0");
        ipcId.add("1");
        searchOption.setDeviceIds(ipcId);
        searchOption.setSearchType(SearchType.PERSON);
        searchOption.setImageId("device-test-1-420620197310111020");
        searchResult = realTimeCompareBySparkSQLTwo.pictureSearchBySparkSQL(searchOption);
        System.out.println(searchResult);
    }

}

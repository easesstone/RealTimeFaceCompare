package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class RealTimeCompare implements Serializable {
    private static byte[] image;// 图片的二进制数据
    private static String imageId;//图片 id ,优先使用图片流数组
    private static float threshold;//阈值
    private static String sortParams;//排序参数
    private static int offset;//分页查询开始行
    private static int count;//分页查询条数
    private static String searchId;//查询Id 由UUID生成
    private static DynamicPhotoService dynamicPhotoService;
    private static List<String> imageIdList;//用于保存筛选出来的一组一个图片的id
    private static List<CapturedPicture> capturedPictures;//图片对象列表
    private static HashMap<String, Float> imgSimilarityMap;//图片Id和相似度的映射关系
    private static SearchResult searchResult;//查询结果
    private static List<float[]> feaFloatList;
    private static List<Float> simList;

    static {
        dynamicPhotoService = new DynamicPhotoServiceImpl();
    }

    private Logger LOG = Logger.getLogger(RealTimeCompare.class);

    SearchResult pictureSearch(SearchOption option) throws Exception {
        long startTimeFirst = System.currentTimeMillis();
        if (null != option) {
            SearchType searchType = option.getSearchType();
            imageId = option.getImageId();
            image = option.getImage();
            String plateNumber = option.getPlateNumber();
            threshold = option.getThreshold();
            List<String> deviceIds = option.getDeviceIds();
            String platformId = option.getPlatformId();
            Date startDate = option.getStartDate();
            Date endDate = option.getEndDate();
            List<TimeInterval> intervals = option.getIntervals();
            sortParams = option.getSortParams();
            List<SearchFilter> filters = option.getFilters();
            offset = option.getOffset();
            count = option.getCount();
            //设置查询Id
            searchId = UuidUtil.setUuid();
            if (null != searchType) {
                //查询的对象库是人
                if (searchType == SearchType.PERSON) {
                    PictureType pictureType = PictureType.PERSON;
                    //上传的参数中有图
                    if (null != image && image.length > 0) {
                        searchResult = compareByImage(pictureType, option);
                    } else {
                        //无图，有imageId
                        if (null != imageId) {
                            searchResult = compareByImageId(pictureType, option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            searchResult = compareByOthers(pictureType, option);
                        }
                    }
                }
                //查询的对象库是车
                else if (searchType == SearchType.CAR) {
                    PictureType pictureType = PictureType.CAR;
                    //上传的参数中有图
                    if (null != image && image.length > 0) {
                        searchResult = compareByImage(pictureType, option);
                    } else {
                        //无图，有imageId
                        if (null != imageId) {
                            searchResult = compareByImageId(pictureType, option);
                        } else {
                            //无图无imageId,通过其他参数查询
                            searchResult = compareByOthers(pictureType, option);
                        }
                    }
                }
            } else {
                LOG.error("SearchType is null");
                searchResult = null;
            }
        } else {
            LOG.error("search parameter option is null");
            searchResult = null;
        }
        System.out.println("查询总时间：" + (System.currentTimeMillis() - startTimeFirst));
        return searchResult;
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片rowkey
     */
    private SearchResult compareByImage(PictureType pictureType, SearchOption option) {
        //对上传的图片提取特征
        float[] searchFea = FaceFunction.featureExtract(image);
        if (null != searchFea && searchFea.length == 512) {
            //将图片特征插入到特征库
            boolean insertStatus = dynamicPhotoService.upPictureInsert(pictureType, searchId, searchFea, image);
            if (insertStatus) {
                LOG.info("feature insert into HBase successful");
            } else {
                LOG.error("feature insert into HBase failed");
            }
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            imageIdList = getImageIdListFromHBase(option);
            if (null != imageIdList && imageIdList.size() > 0) {
                //根据imageId找出对应特征加入组成二元组并加入到列表
                try {
                    feaFloatList = getFeaByImageId(imageIdList, pictureType);
                } catch (Exception e) {
                    LOG.error("insert feature to HBase failed");
                }
                //对上传图片的特征与查询到的满足条件的图片特征进行比对
                simList = featureCompare(searchFea, feaFloatList);
                //根据阈值对计算结果进行过滤，并进行排序分页等操作
                searchResult = lastResult(imageIdList, simList, threshold, pictureType.getType(), sortParams);
            } else {
                Log.info("imageIdFeaTupList is null");
            }
        } else {
            LOG.info("no image find in HBase satisfy the search option");
        }
        return searchResult;
    }

    /**
     * 根据图片id进行搜图的方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片rowkey
     */
    private SearchResult compareByImageId(PictureType pictureType, SearchOption option) {
        //根据imageId从动态库中获取特征值
        long startTime = System.currentTimeMillis();
        byte[] fea = dynamicPhotoService.getFeature(imageId, pictureType);
        if (null != fea && fea.length == 2048) {
            //将获取到的特征从 byte[] 转化为float[]
            float[] searchFea = FaceFunction.byteArr2floatArr(fea);
            if (null != searchFea && searchFea.length == 512) {
                //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
                imageIdList = getImageIdListFromHBase(option);
                if (null != imageIdList && imageIdList.size() > 0) {
                    //根据imageId找出对应特征加入组成二元组并加入到列表
                    try {
                        feaFloatList = getFeaByImageId(imageIdList, pictureType);
                    } catch (Exception e) {
                        LOG.error("Failed to get feature by imageId", e);
                    }
                    //对特征进行比对
                    simList = featureCompare(searchFea, feaFloatList);
                    //根据阈值对计算结果进行过滤，并进行排序分页等操作
                    searchResult = lastResult(imageIdList, simList, threshold, pictureType.getType(), sortParams);
                } else {
                    Log.info("imageIdFeaTupList is null");
                }
            } else {
                LOG.info("No image find in HBase satisfy the search option");
            }
        } else {
            LOG.error("the feature float[] is null or short than 512");
        }
        System.out.println("通过图片查询的时间：" + (System.currentTimeMillis() - startTime));
        return searchResult;
    }

    /**
     * 无图/图片id，仅通过设备、时间等参数进行搜图
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回满足所有查询条件的图片rowkey
     */
    private SearchResult compareByOthers(PictureType pictureType, SearchOption option) {
        //对阈值重新赋值
        threshold = Float.MIN_VALUE;
        imgSimilarityMap = new HashMap<>();
        capturedPictures = new ArrayList<>();
        //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
        imageIdList = getImageIdListFromHBase(option);
        if (null != imageIdList && imageIdList.size() > 0) {
            capturedPictures = dynamicPhotoService.getCaptureMessage(imageIdList, pictureType.getType());
            List<CapturedPicture> capturedPictureListNew = new ArrayList<>();
            for (int i = 0; i < imageIdList.size(); i++) {
                CapturedPicture capturedPicture = capturedPictures.get(i);
                capturedPicture.setId(imageIdList.get(i));
                capturedPictureListNew.add(capturedPicture);
            }
            //根据排序参数进行排序
            capturedPictureListNew = sortByParams(capturedPictureListNew, sortParams);
            //进行分页操作
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPictureListNew, offset, count);
            //返回最终结果
            searchResult = new SearchResult();
            //分组返回图片对象
            searchResult.setPictures(subCapturedPictures);
            //searchId 设置为imageId（rowkey）
            searchResult.setSearchId(searchId);
            //设置查询到的总得记录条数
            searchResult.setTotal(capturedPictures.size());
            System.out.println("list size:" + capturedPictures.size());
            //保存到Hbase
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, imgSimilarityMap);
            if (flag) {
                LOG.info("The search history of: " + searchId + " saved successful");
                System.out.println("查询记录保存成功");
            } else {
                LOG.error("The search history of: " + searchId + " saved failure");
                System.out.println("查询记录保存失败");
            }
        } else {
            LOG.info("Find no image by deviceIds or timeStamp");
        }
        return searchResult;
    }

    /**
     * 通过elasticSearch根据查询参数对HBase数据库进行过滤
     *
     * @param option 查询参数
     * @return 返回满足所有查询条件的结果
     */
    private List<String> getImageIdListFromHBase(SearchOption option) {
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        List<String> rowKeyList = filterByRowkey.getRowKey(option);
        return rowKeyList;
    }

    /**
     * 通过图片id(rowkey)从对应库中查询其特征值
     *
     * @param imageIdList 图片id列表
     * @param pictureType 图片类型（人、车）
     * @return 图片id及其特征所组成的二元组列表
     */
    private List<float[]> getFeaByImageId(List<String> imageIdList, final PictureType pictureType) {
        //根据imageId进行特征查询并转化为float[]
        feaFloatList = dynamicPhotoService.getFeature(imageIdList, pictureType);
        return feaFloatList;
    }

    /**
     * 进行特征比对的方法
     *
     * @param searchFea    查询图片的特征
     * @param feaFloatList 查询到的图片的id以及其特征二元组列表
     * @return 查询到的图片的id以及其与查询图片的相似度二元组列表
     */
    private List<Float> featureCompare(final float[] searchFea, List<float[]> feaFloatList) {
        simList = new ArrayList<>();
        float similarity;
        //对两个特征进行比对
        for (float[] feaFloat : feaFloatList) {
            similarity = FaceFunction.featureCompare(searchFea, feaFloat);
            simList.add(similarity);
        }
        return simList;
    }

    /**
     * 对计算结果根据阈值过滤，根据排序参数排序，分页
     *
     * @param simList    计算得到图片id以及其与查询图片的相似度二元组列表
     * @param threshold  相似度阈值
     * @param type       图片类型
     * @param sortParams 排序参数
     * @return 阈值过滤、排序、分页后最终返回结果
     */
    private SearchResult lastResult(List<String> imageIdList, List<Float> simList, final float threshold, final int type, String sortParams) {
        imgSimilarityMap = new HashMap<>();
        capturedPictures = new ArrayList<>();
        //根据阈值对imageIdSimTupRDD进行过滤，返回大于相似度阈值的结果
        List<String> imageIdFilterList = new ArrayList<>();
        List<Float> simFilterList = new ArrayList<>();
        //采用批量读取的方式直接从HBase读取数据
        for (int i = 0; i < imageIdList.size(); i++) {
            if (simList.get(i) > threshold) {
                imageIdFilterList.add(imageIdList.get(i));
                simFilterList.add(simList.get(i));
                imgSimilarityMap.put(imageIdList.get(i), simList.get(i));
            }
        }
        capturedPictures = dynamicPhotoService.getCaptureMessage(imageIdFilterList, type);
        //设置图片相似度
        List<CapturedPicture> capturedPictureListNew = new ArrayList<>();
        for (int i = 0; i < imageIdFilterList.size(); i++) {
            CapturedPicture capturedPicture = capturedPictures.get(i);
            capturedPicture.setId(imageIdFilterList.get(i));
            capturedPicture.setSimilarity(simFilterList.get(i));
            capturedPictureListNew.add(capturedPicture);
        }
        //根据排序参数进行排序
        capturedPictureListNew = sortByParams(capturedPictureListNew, sortParams);
        //进行分页操作
        List<CapturedPicture> subCapturedPictures = pageSplit(capturedPictureListNew, offset, count);
        //返回最终结果
        searchResult = new SearchResult();
        //分组返回图片对象
        searchResult.setPictures(subCapturedPictures);
        //searchId 设置为imageId（rowkey）
        searchResult.setSearchId(searchId);
        //设置查询到的总得记录条数
        searchResult.setTotal(capturedPictures.size());
        //保存到Hbase
        boolean flag = dynamicPhotoService.insertSearchRes(searchId, imgSimilarityMap);
        if (flag) {
            LOG.info("The search history of: " + searchId + " saved successful");
            System.out.println("查询记录保存成功");
        } else {
            LOG.error("The search history of: " + searchId + " saved failure");
            System.out.println("查询记录保存失败");
        }
        return searchResult;
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
        //内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
        if (null != sortParams && sortParams.length() > 0) {
            //根据自定义的排序方法进行排序
            ListUtils.sort(capturedPictures, sortParam.getSortNameArr(), sortParam.getIsAscArr());
        } else {
            Log.info("sortParams is null!");
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
}


package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import com.hzgc.util.UuidUtil;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单机版
 */
public class RealTimeCompare implements Serializable {
    private Logger LOG = Logger.getLogger(RealTimeCompare.class);
    private byte[] image;// 图片的二进制数据
    private String imageId;//图片 id ,优先使用图片流数组
    private float threshold = Float.MIN_VALUE;//阈值
    private String sortParams;//排序参数
    private int offset;//分页查询开始行
    private int count;//分页查询条数
    private String searchId;//查询Id 由UUID生成
    private DynamicPhotoService dynamicPhotoService;
    private List<String> imageIdList;//用于保存筛选出来的一组一个图片的id
    private List<String> imageIdFilterList;//过滤大图后的图片Id列表
    private HashMap<String, Float> imgSimilarityMap;//图片Id和相似度的映射关系
    private SearchResult searchResult;//查询结果，最终的返回值
    private List<float[]> feaFloatList;//特征列表，根据rowKeyList批量查询到的特征
    private List<Float> simList;//相似度列表，保存比对后的相似度
    private List<CapturedPicture> capturedPictureList;//图片对象列表

    public RealTimeCompare() {
        dynamicPhotoService = new DynamicPhotoServiceImpl();
    }

    SearchResult pictureSearch(SearchOption option) throws Exception {
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
            imgSimilarityMap = new HashMap<>();
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
            } else {//searchType 为空，则同时返回人、车
                //通过es查询到的人脸图片id列表
                List<String> personImageIdList;
                //过滤掉大图后的人脸图片id列表
                List<String> personImageIdFilterList;
                //通过es查询到的车辆图片id列表
                List<String> carImageIdList = null;
                //过滤掉大图后的车辆图片id列表
                List<String> carImageIdFilterList;
                List<String> personUnionCarList = new ArrayList<>();
                PictureType pictureType;
                List<CapturedPicture> capturedPictureList = new ArrayList<>();
                option.setSearchType(SearchType.PERSON);
                personImageIdList = getImageIdListFromEs(option);
                // TODO: 2017-8-31 添加车辆
                if (null != personImageIdList && personImageIdList.size() > 0) {
                    //采用java8stream进行过滤掉大图
                    personImageIdFilterList = personImageIdList.parallelStream().filter(id -> !id.endsWith("_00")).collect(Collectors.toList());
                    if (null != personImageIdFilterList && personImageIdList.size() > 0) {
                        personUnionCarList.addAll(personImageIdFilterList);
                        pictureType = PictureType.PERSON;
                        List<CapturedPicture> capturedPicturesPerson = dynamicPhotoService.getMultiBatchCaptureMessage(personImageIdFilterList, pictureType.getType());
                        if (null != capturedPicturesPerson) {
                            capturedPictureList.addAll(capturedPicturesPerson);
                        }
                    }
                }
                if (null != carImageIdList && carImageIdList.size() > 0) {
                    carImageIdFilterList = carImageIdList.parallelStream().filter(id -> !id.endsWith("_00")).collect(Collectors.toList());
                    if (null != carImageIdFilterList && carImageIdFilterList.size() > 0) {
                        personUnionCarList.addAll(carImageIdFilterList);
                        pictureType = PictureType.CAR;
                        List<CapturedPicture> capturedPicturesPerson = dynamicPhotoService.getMultiBatchCaptureMessage(carImageIdFilterList, pictureType.getType());
                        if (null != capturedPicturesPerson) {
                            capturedPictureList.addAll(capturedPicturesPerson);
                        }
                    }
                }
                for (int i = 0; i < personUnionCarList.size(); i++) {
                    String imageId = personUnionCarList.get(i);
                    imgSimilarityMap.put(imageId, 0.00f);
                }
                searchResult = getLastSearchResult(capturedPictureList, sortParams);
            }
        } else {
            LOG.error("search parameter option is null");
            searchResult = null;
        }
        return searchResult;
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片
     */
    private SearchResult compareByImage(PictureType pictureType, SearchOption option) {
        //对上传的图片提取特征
        float[] searchFea = FaceFunction.featureExtract(image);
        if (null != searchFea && searchFea.length == 512) {
            //将图片特征插入到特征库
            long featureSaveTime = System.currentTimeMillis();
            boolean insertStatus = dynamicPhotoService.upPictureInsert(pictureType, searchId, searchFea, image);
            LOG.info("特征插入到HBase时间消耗：" + (System.currentTimeMillis() - featureSaveTime));
            if (insertStatus) {
                LOG.info("feature[" + searchId + "]insert into HBase successful");
            } else {
                LOG.error("feature[" + searchId + "] insert into HBase failed");
            }
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            long esTime = System.currentTimeMillis();
            imageIdList = getImageIdListFromEs(option);
            LOG.info("从es中筛选图片Id的数量" + imageIdList.size() + " ,时间消耗：" + (System.currentTimeMillis() - esTime));
            long filterSpicTime = System.currentTimeMillis();
            //采用java 8
            if (null != imageIdList && imageIdList.size() > 0) {
                imageIdFilterList = imageIdList.parallelStream().filter(id -> !id.endsWith("_00")).collect(Collectors.toList());
                //采用普通iterator方法
                /*Iterator it = personImageIdList.iterator();
                while (it.hasNext()) {
                    String tempImageId = it.next().toString();
                    if (tempImageId.endsWith("_00")) {
                        it.remove();
                    }
                }*/
                LOG.info("过滤出小图的数量：" + imageIdFilterList.size() + " ,时间消耗：" + (System.currentTimeMillis() - filterSpicTime));
                if (null != imageIdFilterList && imageIdFilterList.size() > 0) {
                    //根据imageId找出对应特征加入组成二元组并加入到列表
                    try {
                        long getFeaTime = System.currentTimeMillis();
                        feaFloatList = getFeaByImageId(imageIdFilterList, pictureType);
                        LOG.info("从HBase中获取特征的数量：" + feaFloatList.size() + ",时间消耗：" + (System.currentTimeMillis() - getFeaTime));
                    } catch (Exception e) {
                        LOG.error("get float[] feature failed by getFeaByImageId method");
                    }
                    //对上传图片的特征与查询到的满足条件的图片特征进行比对
                    long compareTime = System.currentTimeMillis();
                    if (null != feaFloatList && feaFloatList.size() > 0) {
                        simList = featureCompare(searchFea, feaFloatList);
                    } else {
                        LOG.info("feaFloatList is null");
                    }
                    LOG.info("特征比对数量：" + feaFloatList.size() + " ,时间消耗：" + (System.currentTimeMillis() - compareTime));
                    //根据阈值对计算结果进行过滤，并进行排序分页等操作
                    searchResult = lastResult(imageIdFilterList, simList, threshold, pictureType.getType(), sortParams);
                } else {
                    LOG.info("imageIdFilterList is null");
                }
            } else {
                LOG.info("the imageIdList is null");
            }

        } else {
            LOG.info("search feature is null or short than 512");
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
        byte[] fea = dynamicPhotoService.getFeature(imageId, pictureType);
        if (null != fea && fea.length == 2048) {
            //将获取到的特征从 byte[] 转化为float[]
            float[] searchFea = FaceFunction.byteArr2floatArr(fea);
            if (null != searchFea && searchFea.length == 512) {
                //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
                imageIdList = getImageIdListFromEs(option);
                //过滤“—00结尾”的大图
                if (null != imageIdList && imageIdList.size() > 0) {
                    imageIdFilterList = imageIdList.parallelStream().filter(id -> !id.endsWith("_00")).collect(Collectors.toList());
                    if (null != imageIdFilterList && imageIdFilterList.size() > 0) {
                        //根据imageId找出对应特征加入组成二元组并加入到列表
                        try {
                            feaFloatList = getFeaByImageId(imageIdFilterList, pictureType);
                        } catch (Exception e) {
                            LOG.error("Failed to get feature by imageId", e);
                        }
                        //对特征进行比对
                        if (null != feaFloatList && feaFloatList.size() > 0) {
                            simList = featureCompare(searchFea, feaFloatList);
                            //根据阈值对计算结果进行过滤，并进行排序分页等操作
                            if (null != simList && simList.size() > 0) {
                                searchResult = lastResult(imageIdFilterList, simList, threshold, pictureType.getType(), sortParams);
                            } else {
                                LOG.info("simList is null");
                            }
                        } else {
                            LOG.error("all the feature of imageIdList is null");
                        }

                    } else {
                        LOG.info("imageIdFilterList is null");
                    }
                } else {
                    LOG.info("the imageIdList is null");
                }
            }
        } else {
            LOG.error("the feature read from HBase is null or short than 2048");
        }
        return searchResult;
    }

    /**
     * 无图/图片id，仅通过设备、时间等参数进行搜图
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回满足所有查询条件的图片
     */
    private SearchResult compareByOthers(PictureType pictureType, SearchOption option) {
        //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
        imageIdList = getImageIdListFromEs(option);
        //过滤掉大图
        if (null != imageIdList && imageIdList.size() > 0) {
            List<String> imageIdFilterList = imageIdList.parallelStream().filter(id -> !id.endsWith("_00")).collect(Collectors.toList());
         /*Iterator it = personImageIdList.iterator();
                while (it.hasNext()) {
                    String tempImageId = it.next().toString();
                    if (tempImageId.endsWith("_00")) {
                        it.remove();
                    }
                }*/
            if (null != imageIdFilterList && imageIdFilterList.size() > 0) {
                //capturedPictureList = dynamicPhotoService.getBatchCaptureMessage(imageIdList, pictureType.getType());
                capturedPictureList = dynamicPhotoService.getMultiBatchCaptureMessage(imageIdFilterList, pictureType.getType());
                searchResult = getLastSearchResult(capturedPictureList, sortParams);
            } else {
                LOG.info("imageIdFilterList is null");
            }
        } else {
            LOG.info("imageIdList is null");
        }
        return searchResult;
    }

    /**
     * 通过elasticSearch根据查询参数对HBase数据库进行过滤
     *
     * @param option 查询参数
     * @return 返回满足所有查询条件的结果
     */
    private List<String> getImageIdListFromEs(SearchOption option) {
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        return filterByRowkey.getRowKey(option);
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
        //feaFloatList = dynamicPhotoService.getBatchFeature(imageIdList, pictureType);
        feaFloatList = dynamicPhotoService.getMultiBatchFeature(imageIdList, pictureType);
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
        for (int i = 0; i < feaFloatList.size(); i++) {
            float[] feaFloat = feaFloatList.get(i);
            if (null != feaFloat && feaFloat.length == 512) {
                similarity = FaceFunction.featureCompare(searchFea, feaFloat);
                simList.add(similarity);
            } else {
                similarity = 0.00f;
                simList.add(similarity);
            }
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
        //根据阈值对imageIdSimTupRDD进行过滤，返回大于相似度阈值的结果
        List<String> imageIdFilterList = new ArrayList<>();
        List<Float> simFilterList = new ArrayList<>();
        SearchResult searchResultLast;
        long thresholdTime = System.currentTimeMillis();
        for (int i = 0; i < imageIdList.size(); i++) {
            if (simList.get(i) > threshold) {
                imageIdFilterList.add(imageIdList.get(i));
                simFilterList.add(simList.get(i));
                imgSimilarityMap.put(imageIdList.get(i), simList.get(i));
            }
        }
        LOG.info("根据相似度过滤imageId的数量：" + imgSimilarityMap.size() + " ,时间消耗：" + (System.currentTimeMillis() - thresholdTime));
        long getMultiBatchCaptureMessageTime = System.currentTimeMillis();
        //批量读取
        //capturedPictureList = dynamicPhotoService.getBatchCaptureMessage(imageIdFilterList, type);
        //多线程批量读取
        capturedPictureList = dynamicPhotoService.getMultiBatchCaptureMessage(imageIdFilterList, type);
        //设置图片相似度
        List<CapturedPicture> capturedPictureListTemp = new ArrayList<>();
        for (int i = 0; i < imageIdFilterList.size(); i++) {
            CapturedPicture capturedPicture = capturedPictureList.get(i);
            capturedPicture.setSimilarity(simFilterList.get(i));
            capturedPictureListTemp.add(capturedPicture);
        }
        LOG.info("根据ImageId多线程批量获取图片对象的数量：" + capturedPictureListTemp.size() + " ,时间消耗：" + (System.currentTimeMillis() - getMultiBatchCaptureMessageTime));
        searchResultLast = getLastSearchResult(capturedPictureListTemp, sortParams);
        return searchResultLast;
    }

    /**
     * 根据阈值过滤后的imageIdList批量查询数据对象分组排
     *
     * @param capturedPictures 根据阈值过滤之后的对象列表
     * @return 最终查询结果
     */
    private SearchResult getLastSearchResult(List<CapturedPicture> capturedPictures, String sortParams) {
        SearchResult searchResultTemp = new SearchResult();
        List<CapturedPicture> capturedPicturesLast;
        if (null != capturedPictures && capturedPictures.size() > 0) {
            //设置图片相似度
            long sortTime = System.currentTimeMillis();
            //根据排序参数进行排序
            capturedPicturesLast = sortByParams(capturedPictures, sortParams);
            LOG.info("对" + capturedPictures.size() + "张图片对象进行排序时间消耗：" + (System.currentTimeMillis() - sortTime));
            long splitTime = System.currentTimeMillis();
            //进行分页操作
            List<CapturedPicture> subCapturedPictures = pageSplit(capturedPicturesLast, offset, count);
            LOG.info("分页返回" + subCapturedPictures.size() + "时间消耗：" + (System.currentTimeMillis() - splitTime));
            //返回最终结果
            searchResultTemp = new SearchResult();
            //分组返回图片对象
            searchResultTemp.setPictures(subCapturedPictures);
            //searchId 设置为imageId（rowkey）
            searchResultTemp.setSearchId(searchId);
            //设置查询到的总得记录条数
            searchResultTemp.setTotal(capturedPictures.size());
            long saveSearchTime = System.currentTimeMillis();
            //保存到Hbase
            boolean flag = dynamicPhotoService.insertSearchRes(searchId, imgSimilarityMap);
            LOG.info("保存查询结果时间消耗：" + (System.currentTimeMillis() - saveSearchTime));
            if (flag) {
                LOG.info("The search history of: [" + searchId + "] saved successful");
            } else {
                LOG.error("The search history of: [" + searchId + "] saved failure");
            }
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
        //内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
        if (null != sortParams && sortParams.length() > 0) {
            //根据自定义的排序方法进行排序
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
}


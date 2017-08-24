package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.dubbo.dynamicrepo.DynamicPhotoService;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.DateUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static com.hzgc.util.ObjectUtil.byteToObject;
import static com.hzgc.util.ObjectUtil.objectToByte;

/**
 * 动态库实现类
 */
public class DynamicPhotoServiceImpl implements DynamicPhotoService {
    private static Logger LOG = Logger.getLogger(DynamicPhotoServiceImpl.class);

    static {
        HBaseHelper.getHBaseConnection();
    }

    /**
     * 将rowKey、特征值插入人脸/车辆库 （内）（刘思阳）
     * 表名：person/car
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertPictureFeature(PictureType type, String rowKey, float[] feature) {
        if (null != rowKey && type == PictureType.PERSON) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                person.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_person failed! used method DynamicPhotoServiceImpl.insertePictureFeature.");
            } finally {
                HBaseUtil.closTable(person);
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                car.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_car failed! used method DynamicPhotoServiceImpl.insertPictureFeature.");
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.insertPictureFeature param is empty.");
        }
        return false;
    }

    /**
     * 根据小图rowKey获取小图特征值 （内）（刘思阳）
     * 表名：person/car
     *
     * @param imageId 小图rowKey
     * @param type    人/车
     * @return byte[] 小图特征值
     */
    @Override
    public byte[] getFeature(String imageId, PictureType type) {
        byte[] feature = null;
        if (null != imageId) {
            Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            Get get = new Get(Bytes.toBytes(imageId));
            if (type == PictureType.PERSON) {
                try {
                    Result result = personTable.get(get);
                    feature = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_person failed! used method FilterByRowKey.getSmallImage");
                }
            } else if (type == PictureType.CAR) {
                try {
                    Result result = carTable.get(get);
                    feature = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_car failed! used method FilterByRowKey.getSmallImage");
                }
            }
        } else {
            LOG.error("method FilterByRowKey.getFeature param is empty");
        }
        return feature;
    }

    public List<float[]> getFeature(List<String> imageIdList, PictureType type) {
        List<float[]> feaFloatList = new ArrayList<>();
        if (null != imageIdList && imageIdList.size() > 0) {
            Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            List<Get> gets = new ArrayList<Get>();
            for (String imageId : imageIdList) {
                Get get = new Get(Bytes.toBytes(imageId));
                //get.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"));
                gets.add(get);
            }
            if (type == PictureType.PERSON) {
                try {
                    Result[] results = personTable.get(gets);
                    for (Result result : results) {
                        float[] featureFloat = FaceFunction.byteArr2floatArr(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA));
                        feaFloatList.add(featureFloat);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_person failed! used method FilterByRowKey.getSmallImage");
                }
            } else if (type == PictureType.CAR) {
                try {
                    Result[] results = carTable.get(gets);
                    for (Result result : results) {
                        float[] featureFloat = FaceFunction.byteArr2floatArr(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_FEA));
                        feaFloatList.add(featureFloat);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_car failed! used method FilterByRowKey.getSmallImage");
                }
            }
        } else {
            LOG.error("method FilterByRowKey.getFeature param is empty");
        }
        return feaFloatList;
    }

    /**
     * 将上传的图片、rowKey、特征值插入人脸/车辆特征库 （内）
     * 表名：upFea
     *
     * @param type    人/车
     * @param rowKey  上传图片ID（rowKey）
     * @param feature 特征值
     * @param image   图片
     * @return boolean 是否插入成功
     */
    @Override
    public boolean upPictureInsert(PictureType type, String rowKey, float[] feature, byte[] image) {
        Table table = HBaseHelper.getTable(DynamicTable.TABLE_UPFEA);
        if (null != rowKey && type == PictureType.PERSON) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_SMALLIMAGE, Bytes.toBytes(Arrays.toString(image)));
                put.addColumn(DynamicTable.UPFEA_PERSON_COLUMNFAMILY, DynamicTable.UPFEA_PERSON_COLUMN_FEA, Bytes.toBytes(featureStr));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_person failed! used method DynamicPhotoServiceImpl.insertPictureFeature.");
            }
        } else if (null != rowKey && type == PictureType.CAR) {
            try {
                String featureStr = FaceFunction.floatArray2string(feature);
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_FEA, Bytes.toBytes(featureStr));
                put.addColumn(DynamicTable.UPFEA_CAR_COLUMNFAMILY, DynamicTable.UPFEA_CAR_COLUMN_SMALLIMAGE, Bytes.toBytes(Arrays.toString(image)));
                table.put(put);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("insert feature by rowKey from table_car failed! used method DynamicPhotoServiceImpl.upPictureInsert.");
            }
        } else {
            LOG.error("method DynamicPhotoServiceImpl.upPictureInsert param is empty.");
        }
        return false;
    }

    /**
     * 将查询ID、查询相关信息插入查询结果库 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchId 查询ID（rowKey）
     * @param resList  查询信息（返回图片ID、相识度）
     * @return boolean 是否插入成功
     */
    @Override
    public boolean insertSearchRes(String searchId, Map<String, Float> resList) {
        Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        try {
            Put put = new Put(Bytes.toBytes(searchId));
            byte[] searchMessage = objectToByte(resList);
            put.addColumn(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE, searchMessage);
            searchRes.put(put);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("insert data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.insertSearchRes.");
        }
        return false;
    }

    /**
     * 根据动态库查询ID获取查询结果 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchID 查询ID（rowKey）
     * @return search结果数据列表
     */
    @Override
    public Map<String, Float> getSearchRes(String searchID) {
        Map<String, Float> searchMessageMap = new HashMap<>();
        Table searchRes = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Get get = new Get(Bytes.toBytes(searchID));
        try {
            Result result = searchRes.get(get);
            byte[] searchMessage = result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHMESSAGE);
            searchMessageMap = (Map<String, Float>) byteToObject(searchMessage);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("get data by searchId from table_searchRes failed! used method DynamicPhotoServiceImpl.getSearchRes.");
        }
        return searchMessageMap;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowkey）
     * @param type    图片类型，人/车
     * @return DynamicObject    动态库对象
     */
    public CapturedPicture getCaptureMessage(String imageId, int type) {
        CapturedPicture capturedPicture = new CapturedPicture();
        if (null != imageId) {
            capturedPicture.setId(imageId);
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            Map<String, Object> mapEx = new HashMap<>();
            if (type == PictureType.PERSON.getType()) {
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);
                    setCapturedPicture_person(capturedPicture, result, mapEx);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get CapturedPicture by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 6");
                }
            } else {
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = car.get(get);
                    setCapturedPicture_car(capturedPicture, result, mapEx);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get CapturedPicture by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.case 7");
                }
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage param is empty.");
        }
        return capturedPicture;
    }

    /**
     * 批量查询图片对象
     *
     * @param imageIdList 图片Id列表
     * @param type        搜索类型
     * @return 图片对象列表
     */
    @Override
    public List<CapturedPicture> getCaptureMessage(List<String> imageIdList, int type) {
        Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
        Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
        List<CapturedPicture> capturedPictureList = new ArrayList<>();
        List<Get> gets = new ArrayList<Get>();
        Map<String, Object> mapEx = new HashMap<>();
        for (String imageId : imageIdList) {
            Get get = new Get(Bytes.toBytes(imageId));
            gets.add(get);
        }
        CapturedPicture capturedPicture;
        try {
            if (type == PictureType.PERSON.getType()) {
                Result[] results = person.get(gets);
                for (Result result : results) {
                    capturedPicture = new CapturedPicture();
                    setCapturedPicture_person(capturedPicture, result, mapEx);
                    capturedPictureList.add(capturedPicture);
                    System.out.println(capturedPicture);
                }
            } else {
                Result[] results = car.get(gets);
                for (Result result : results) {
                    capturedPicture = new CapturedPicture();
                    setCapturedPicture_person(capturedPicture, result, mapEx);
                    capturedPictureList.add(capturedPicture);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return capturedPictureList;
    }

    private void setCapturedPicture_person(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
        capturedPicture.setDescription(des);

        String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
        mapEx.put("ex", ex);
        capturedPicture.setExtend(mapEx);

        //不从rowkey解析，直接从数据库中读取ipcId和timestamp
        String ipcId = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID));
        capturedPicture.setIpcId(ipcId);

        String time = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP));
        long timeStamp = DateUtil.dateToTimeStamp(time);
        capturedPicture.setTimeStamp(timeStamp);
    }

    private void setCapturedPicture_car(CapturedPicture capturedPicture, Result result, Map<String, Object> mapEx) {
        String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
        capturedPicture.setDescription(des);

        String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
        mapEx.put("ex", ex);
        capturedPicture.setExtend(mapEx);

        //不从rowkey解析，直接从数据库中读取ipcId和timestamp
        String ipcId = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IPCID));
        capturedPicture.setIpcId(ipcId);

        String time = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_TIMESTAMP));
        long timeStamp = DateUtil.dateToTimeStamp(time);
        capturedPicture.setTimeStamp(timeStamp);

        String plateNumber = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM));
        capturedPicture.setPlateNumber(plateNumber);
    }
}

package com.hzgc.cluster.consumer;

import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.hbase.dynamicrepo.DynamicTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.jni.FaceAttribute;
import org.elasticsearch.action.index.IndexResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PutDataToEs implements Serializable {
    private static PutDataToEs instance = null;

    public static PutDataToEs getInstance() {
        if (instance == null) {
            synchronized (PutDataToEs.class) {
                if (instance == null) {
                    instance = new PutDataToEs();
                }
            }
        }
        return instance;
    }

    public int putDataToEs(String ftpurl, FaceObject faceObject) {
        String timestamp = faceObject.getTimeStamp();
        String ipcid = faceObject.getIpcId();
        String timeslot = faceObject.getTimeSlot();
        String date = faceObject.getDate();
        IndexResponse indexResponse = new IndexResponse();
        String pictype = faceObject.getType().name();
        Map<String, Object> map = new HashMap<>();
        FaceAttribute faceAttr = faceObject.getAttribute();
        int haircolor = faceAttr.getHairColor();
        map.put("haircolor", haircolor);
        int eleglasses = faceAttr.getEyeglasses();
        map.put("eleglasses", eleglasses);
        int gender = faceAttr.getGender();
        map.put("gender", gender);
        int hairstyle = faceAttr.getHairStyle();
        map.put("hairstyle", hairstyle);
        int hat = faceAttr.getHat();
        map.put("hat", hat);
        int huzi = faceAttr.getHuzi();
        map.put("huzi", huzi);
        int tie = faceAttr.getTie();
        map.put("tie", tie);
        map.put("date", date);
        map.put("pictype", pictype);
        map.put("timestamp", timestamp);
        map.put("ipcid", ipcid);
        map.put("timeslot", timeslot);
        if (ftpurl != null) {
            indexResponse = ElasticSearchHelper.getEsClient().prepareIndex(DynamicTable.DYNAMIC_INDEX,
                    DynamicTable.PERSON_INDEX_TYPE, ftpurl).setSource(map).get();
        }
        if (indexResponse.getVersion() == 1) {
            return 1;
        } else {
            return 0;
        }
    }
}

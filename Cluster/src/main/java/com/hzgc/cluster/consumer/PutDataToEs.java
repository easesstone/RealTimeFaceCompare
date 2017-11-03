package com.hzgc.cluster.consumer;

import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.hbase.dynamicrepo.DynamicTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.jni.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import org.elasticsearch.action.index.IndexResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PutDataToEs implements Serializable {
    public int putDataToEs(String ftpurl, FaceObject faceObject) {
        String timestamp = faceObject.getTimeStamp();
        String ipcid = faceObject.getIpcId();
        String timeslot = faceObject.getTimeSlot();
        String date = faceObject.getDate();
        IndexResponse indexResponse = new IndexResponse();
        String picType = faceObject.getType().name();
        Map<String, Object> map = new HashMap<>();
        FaceAttribute faceAttr = faceObject.getAttribute();
        float[] feature = faceAttr.getFeature();
        String newfeature = FaceFunction.floatArray2string(feature);
        map.put("feature", newfeature);
        String haircolor = faceAttr.getHairColor().name();
        map.put("haircolor", haircolor);
        String eleglasses = faceAttr.getEyeglasses().name();
        map.put("eleglasses", eleglasses);
        String gender = faceAttr.getGender().name();
        map.put("gender", gender);
        String hairstyle = faceAttr.getHairStyle().name();
        map.put("hairstyle", hairstyle);
        String hat = faceAttr.getHat().name();
        map.put("hat", hat);
        String huzi = faceAttr.getHuzi().name();
        map.put("huzi", huzi);
        String tie = faceAttr.getTie().name();
        map.put("tie", tie);
        map.put("date", date);
        map.put("pictype", picType);
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

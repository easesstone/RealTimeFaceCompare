package com.hzgc.collect.expand.meger;

import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.ftp.util.FtpUtils;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

class GetFaceObject {
    private  Logger LOG = Logger.getLogger(GetFaceObject.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


     static FaceObject getFaceObject(String row) {
        FaceObject faceObject = new FaceObject();
        if (row != null && row.length() != 0) {
            String path = row.substring(row.lastIndexOf(":") + 1, row.lastIndexOf(","));
            String fileName = path.substring(path.indexOf("/"));
            byte[] photo = FaceFunction.inputPicture(fileName);
            FaceAttribute faceAttribute = FaceFunction.featureExtract(photo);
            Map<String, String> map = FtpUtils.getFtpPathMessage(fileName);
            String ipcID = map.get("ipcID");
            String timeStamp = map.get("time");
            String date = map.get("date");
            String timeSlot = map.get("sj");
            faceObject.setIpcId(ipcID);
            faceObject.setTimeStamp(timeStamp);
            faceObject.setTimeSlot(timeSlot);
            faceObject.setDate(date);
            faceObject.setType(SearchType.PERSON);
            faceObject.setStartTime(sdf.format(new Date()));
            faceObject.setImage(photo);
            faceObject.setAttribute(faceAttribute);
        }
        return faceObject;
    }
}

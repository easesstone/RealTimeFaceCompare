package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.jni.NativeFunction;

public class FaceObjectTest {
    public static void main(String[] args) {
        NativeFunction.init();
        String row = "{\"count\":500,\"path\":\"ftp://s100:2121/DS-2CD2T20FD-I320160122AACH571485690/2018/02/01/16/2018_02_01_16_15_03_73602_1.jpg\"," +
                "\"absolutePath\":\"/DS-2CD2T20FD-I320160122AACH571485690/2018/02/01/16/2018_02_01_16_15_03_73602_1.jpg\",\"timeStamp\":1518074471290,\"status\":\"0\"}";
        String ftpUrl = "ftp://s100:2121/DS-2CD2T20FD-I320160122AACH571485690/2018/02/01/16/2018_02_01_16_15_03_73602_1.jpg";
        FaceObject faceObject = GetFaceObject.getFaceObject(row);
        System.out.println(faceObject);
        System.out.println("Attribute-----" + faceObject.getAttribute().toString());
        System.out.println("Data----------" + faceObject.getDate());
        System.out.println("IpcId---------" + faceObject.getIpcId());
        System.out.println("faceObject----" + faceObject.toString());

//        if (faceObject != null) {
//            SendDataToKafka sendDataToKafka = SendDataToKafka.getSendDataToKafka();
//            MergeSendCallback sendCallback = new MergeSendCallback(sendDataToKafka.getFEATURE(), ftpUrl);
//            sendDataToKafka.sendKafkaMessage(ProducerKafka.getFEATURE(), ftpUrl, faceObject, sendCallback);
//
//            boolean success = sendCallback.isFlag();
//
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            if (!success) {
//                System.out.println("****************************Send the message to kafka failed! Rewrite to new merge error file!" + "****************************");
//            } else {
//                System.out.println("#############Send the message to kafka successfully!##################");
//            }
//
//        }
    }
}
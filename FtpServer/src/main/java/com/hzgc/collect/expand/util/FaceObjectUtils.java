package com.hzgc.collect.expand.util;

import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.util.common.IOUtil;

import java.io.*;

public class FaceObjectUtils {

    private FaceObjectUtils() {
    }
    /**
     * 对象转字节数组
     *
     * @param faceObject 人脸属性和特征值对象
     * @return byte[]
     */
    public static byte[] objectToBytes(FaceObject faceObject) {
        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(faceObject);
            bytes = bo.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(bo);
            IOUtil.closeStream(oo);
        }
        return bytes;
    }

    /**
     * 字节数组转对象
     *
     * @param bytes
     * @return
     */
    public static FaceObject bytesToObject(byte[] bytes) {
        FaceObject faceObject = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            faceObject = (FaceObject) oi.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(bi);
            IOUtil.closeStream(oi);
        }
        return faceObject;
    }
}


package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.producer.FaceObject;

import java.io.*;

public class BeanUtils {

    private BeanUtils() {
    }
    /**
     * 对象转字节数组
     *
     * @param faceObject
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
            try {
                if (bo != null) {
                    bo.close();
                }
                if (oo != null) {
                    oo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                if (bi != null) {
                    bi.close();
                }
                if (oi != null) {
                    oi.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return faceObject;
    }
}


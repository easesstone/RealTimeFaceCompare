package com.hzgc.jni;

import com.hzgc.jni.FaceAttr;

import java.io.*;

public class BeanUtils {

    private BeanUtils() {
    }
    /**
     * 对象转字节数组
     *
     * @param faceAttr
     * @return byte[]
     */
    public static byte[] objectToBytes(FaceAttr faceAttr) {
        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(faceAttr);
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
    public static FaceAttr bytesToObject(byte[] bytes) {
        FaceAttr faceAttr = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            faceAttr = (FaceAttr) oi.readObject();

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
        return faceAttr;
    }
}


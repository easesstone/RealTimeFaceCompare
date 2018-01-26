package com.hzgc.util.common;

import java.io.*;

public class ObjectUtil {

    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(bo);
            IOUtil.closeStream(oo);
        }
        return bytes;
    }

    public static <T> T byteToObject(byte[] bc, Class<T> clz) {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bc);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            bis.close();
            ois.close();
        }
        catch(Exception e) {
            System.out.println("translation"+e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(bis);
            IOUtil.closeStream(ois);
        }
        return (T) obj;
    }
}

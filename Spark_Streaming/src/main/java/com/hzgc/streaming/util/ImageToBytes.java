package com.hzgc.streaming.util;

import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class ImageToBytes implements Serializable {
    public static byte[] image2byte(String path) throws IOException {
        File file = new File(path);
        if (file.exists()){
            FileImageInputStream in = new FileImageInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int readByte = 0;
            byte[] tmp = new byte[1024];
            while((readByte = in.read(tmp)) != -1){
                out.write(tmp, 0, readByte);
            }
            byte[] data = out.toByteArray();
            out.close();
            in.close();
            return data;
        }
        return null;
    }
}

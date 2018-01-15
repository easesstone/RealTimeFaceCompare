package com.hzgc.collect.expand.log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        File file = new File("/opt/logwriter.log");
        if (file.exists()) {
            file.delete();
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String str = "count:123456789 id:abcdefghwa name:20180115102212345678 timestamp:1515997122177\r\n";
        File file1 = new File("/opt/logwriter.log");
        FileOutputStream fis = new FileOutputStream(file1,true);
        for (int i = 0; i < 3000000; i++) {

            try {
                fis.write(str.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        System.out.println((end-start)/300000f);
    }
}

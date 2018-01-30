package com.hzgc.collect.expand.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) {
        File file = new File("/opt/");
        System.out.println(file.list());
    }

    static void write() {
        try {
            FileWriter writer = new FileWriter("/opt/random.txt");
            writer.write("abc\n");
//            writer.write(System.getProperty("line.separator"));
            writer.write("efg\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

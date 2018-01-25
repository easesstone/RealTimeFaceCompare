package com.hzgc.collect.expand.log;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String[] args) {
//        try {
//            RandomAccessFile raf = new RandomAccessFile("/opt/raf.txt", "rw");
//            long length = raf.length() - 1;
//            raf.seek(length);
//            if (raf.readByte() == '\n') {
//                System.out.println("asdfasdf");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        String a1 = "0000000000030000";
        String a2 = "0000000000040000";
        String a3 = "0000000000050000";
        String a4 = "0000000000060000";
        String a5 = "0000000000070000";
        String[] arr = new String[]{a1, a2, a3};
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr));

    }

    public String logNameUpdate(String defaultName, long count) {
        char[] oldChar = defaultName.toCharArray();
        char[] content = (count + "").toCharArray();
        for (int i = 0; i < content.length; i++) {
            oldChar[oldChar.length - 1 - i] = content[content.length - 1 - i];
        }
        return new String(oldChar);
    }
}

class Person {
    int id;
    String name;
    double height;

    public Person() {
    }

    public Person(int id, String name, double height) {
        this.id = id;
        this.name = name;
        this.height = height;
    }

    public void write(RandomAccessFile raf) throws IOException {
        raf.write(id);
        raf.writeUTF(name);
        raf.writeDouble(height);
        raf.close();
    }
}

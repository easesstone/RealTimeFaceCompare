package com.hzgc.collect.expand.log;

import java.io.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String[] args) {
        try {
            String str = System.getProperty("line.separator");
            FileWriter writer = new FileWriter("/opt/pp.txt");
            writer.write("pp");
            writer.write(str);
            writer.write("pp");
//            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Member {
    private String name;
    private int age;
    public Member() {
    }
    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return name + ":" + age;
    }
}

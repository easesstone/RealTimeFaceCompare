package com.hzgc.collect.expand.log;

import java.io.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String[] args) {
        long a = -19;
        long b = 19;
        System.out.println(Long.MAX_VALUE);
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

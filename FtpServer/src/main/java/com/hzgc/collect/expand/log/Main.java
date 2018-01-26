package com.hzgc.collect.expand.log;

import com.hzgc.collect.expand.conf.CommonConf;

import java.io.*;

public class Main {
    public static void main(String[] args) {

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

abstract class Person {
    public Person(Class clz) {
        System.out.println(clz.toString());
    }
}

class Zhaozhe extends Person {
    public Zhaozhe() {
        super(Zhaozhe.class);
    }

    public static void main(String[] args) {
        Zhaozhe zhaozhe = new Zhaozhe();

    }
}
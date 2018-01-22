package com.hzgc.collect.expand.log;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public Main(String string){

    }
    public Main(String s1, String s2) {
        this(s1);
    }
     static List<String> integer;
}

class PP {
    public static void main(String[] args) {
        BlockingQueue<String> deque = new ArrayBlockingQueue<String>(2);
        System.out.println(deque.offer("1"));
        System.out.println(deque.offer("2"));
        try {
            deque.put("3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

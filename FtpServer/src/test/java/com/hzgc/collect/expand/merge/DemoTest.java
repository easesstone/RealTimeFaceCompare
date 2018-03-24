package com.hzgc.collect.expand.merge;

import java.util.ArrayList;
import java.util.List;

public class DemoTest {
    public static void main(String[] args) {
        List<String> demos = new ArrayList<>();
        for (String demo : demos) {
            System.out.println("--------");
        }
        new Thread(new ThreadDemo()).start();
        for (int i = 0;i <= 10;i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Main: " + i);
        }

    }
}

class ThreadDemo implements Runnable {

    @Override
    public void run() {
        for (int i = 0;i <= 10;i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i == 7) {
                System.exit(0);
            }
            System.out.println("in thread demo " + i);
        }
    }
}


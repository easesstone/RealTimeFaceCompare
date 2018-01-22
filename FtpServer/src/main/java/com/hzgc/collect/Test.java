package com.hzgc.collect;

import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;

public class Test {
    public static void main(String[] args) {
        Thread thread = new Thread(new PP(args[0]));
        thread.start();
        Thread thread1 = new Thread(new PP(args[0]));
        thread1.start();
        Thread thread2 = new Thread(new PP(args[0]));
        thread2.start();
        Thread thread3 = new Thread(new PP(args[0]));
        thread3.start();
    }
}

class PP implements Runnable {
    private String str;

    public PP(String str) {
        this.str = str;
    }

    @Override
    public void run() {
        NativeFunction.init();
        byte[] pic = FaceFunction.inputPicture(str);
        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            FaceFunction.featureExtract(pic);
            long end = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + ":" + (end - start));

        }
    }
}

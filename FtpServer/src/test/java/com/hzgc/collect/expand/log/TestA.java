package com.hzgc.collect.expand.log;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class TestA {
    @Test
    public void readAllLine() {
        RandomAccessFile raf = null;
        FileChannel channel = null;
        FileLock lock = null;
        try {
            //1. 对于一个只读文件通过任意方式加锁时会报NonWritableChannelException异常
            //2. 无参lock()默认为独占锁，不会报NonReadableChannelException异常，因为独占就是为了写
            //3. 有参lock()为共享锁，所谓的共享也只能读共享，写是独占的，共享锁控制的代码只能是读操作，当有写冲突时会报NonWritableChannelException异常
            //new FileOutputStream("E:\\logfile.txt", true).getChannel();
            raf = new RandomAccessFile("E:\\logfile.txt", "rw");
            //在文件末尾追加内容的处理
            raf.seek(raf.length());
            channel = raf.getChannel();
            //获得锁方法一：lock()，阻塞的方法，当文件锁不可用时，当前进程会被挂起
            lock = channel.lock();//无参lock()为独占锁
            //lock = channel.lock(0L, Long.MAX_VALUE, true);//有参lock()为共享锁，有写操作会报异常
            //获得锁方法二：trylock()，非阻塞的方法，当文件锁不可用时，tryLock()会得到null值
           /* do {
                lock = channel.tryLock();
            } while (null == lock);*/
            //互斥操作
            try {
                //java 8 一行代码读取文件
                List<String> arrayList = Files.readAllLines(Paths.get("E:\\logfile.txt"), StandardCharsets.UTF_8);
                System.out.println("hello");
                for (String arr : arrayList) {
                    System.out.println(arr);
                }
                System.out.println("***************************");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Thread.sleep(10000);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

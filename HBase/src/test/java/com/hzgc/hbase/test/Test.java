package com.hzgc.hbase.test;


import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

public class Test {


    public static void main(String[] args) {
        Test java8tester = new Test();
//        java8tester.testLocalDateTime();
        System.out.println("The name of class Foo is: "+Test.class.getName());
        String a = "hello";
        String b = new String("world");
        if (a.equals(b)){
            System.out.println(true);
        }else{
            System.out.println(false);
        }

        if (Objects.equals(a, b)){
            System.out.println(true);
        }else{
            System.out.println(false);
        }
    }
//    public void testLocalDateTime(){
//        // 获取当前的日期时间
//        LocalDateTime currentTime = LocalDateTime.now();
//        System.out.println("当前时间: " + currentTime);
//
//        LocalDate date1 = currentTime.toLocalDate();
//        System.out.println("date1: " + date1);
//
//        Month month = currentTime.getMonth();
//        int day = currentTime.getDayOfMonth();
//        int seconds = currentTime.getSecond();
//
//        System.out.println("月: " + month +", 日: " + day +", 秒: " + seconds);
//
//        LocalDateTime date2 = currentTime.withDayOfMonth(10).withYear(2012);
//        System.out.println("date2: " + date2);
//
//        // 12 december 2014
//        LocalDate date3 = LocalDate.of(2014, Month.DECEMBER, 12);
//        System.out.println("date3: " + date3);
//
//        // 22 小时 15 分钟
//        LocalTime date4 = LocalTime.of(22, 15);
//        System.out.println("date4: " + date4);
//
//        // 解析字符串
//        LocalTime date5 = LocalTime.parse("20:15:30");
//        System.out.println("date5: " + date5);
//    }
}

package com.hzgc.collect.expand.util;

import com.google.gson.Gson;
import com.hzgc.collect.expand.log.LogEvent;

public class JsonHelper {
    private static Gson gson = new Gson();
    public static String toJson(LogEvent event) {
        return gson.toJson(event);
    }

    public static void main(String[] args) {
        LogEvent logEvent = new LogEvent();
        logEvent.setStatus("1");
        logEvent.setTimeStamp("111");
        System.out.println(JsonHelper.toJson(logEvent));
    }
}

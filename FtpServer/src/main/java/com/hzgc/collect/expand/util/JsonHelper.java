package com.hzgc.collect.expand.util;

import com.google.gson.Gson;
import com.hzgc.collect.expand.log.LogEvent;

public class JsonHelper {
    private static Gson gson = new Gson();
    public static String toJson(LogEvent event) {
        return gson.toJson(event);
    }
}

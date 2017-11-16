package com.hzgc.hbase.putdata;



import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class MapToJson {
    public static String mapToJson(Map<String,Object> map){
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        return jsonStr;
    }
}

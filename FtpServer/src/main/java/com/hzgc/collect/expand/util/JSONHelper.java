package com.hzgc.collect.expand.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object o) {
        String string = null;
        try {
            string = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static <T> T toObject(String jsonData, Class<T> beanType) {
        T t = null;
        try {
            t = mapper.readValue(jsonData.trim(), beanType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> List<T> toArray(String jsonData, Class<T> beanType) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, beanType);
        List<T> list = new ArrayList<>();
        try {
            list = mapper.readValue(jsonData, javaType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

package com.hzgc.collect.expand.processer;

import com.hzgc.collect.util.BeanUtils;

import java.util.Map;


public class FaceObjectEncoder implements org.apache.kafka.common.serialization.Serializer<FaceObject> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, FaceObject faceObject) {
        return BeanUtils.objectToBytes(faceObject);
    }

    @Override
    public void close() {

    }
}

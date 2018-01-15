package com.hzgc.collect.expand.processer;

import com.hzgc.collect.expand.util.FaceObjectUtils;

import java.util.Map;


public class FaceObjectEncoder implements org.apache.kafka.common.serialization.Serializer<FaceObject> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, FaceObject faceObject) {
        return FaceObjectUtils.objectToBytes(faceObject);
    }

    @Override
    public void close() {

    }
}

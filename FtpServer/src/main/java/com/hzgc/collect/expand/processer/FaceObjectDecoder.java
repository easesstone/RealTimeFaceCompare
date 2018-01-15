package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.util.FaceObjectUtils;
import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

public class FaceObjectDecoder implements Decoder<FaceObject> {
    public FaceObjectDecoder(VerifiableProperties verifiableProperties) {
    }
    @Override
    public FaceObject fromBytes(byte[] bytes) {
        return FaceObjectUtils.bytesToObject(bytes);
    }
}

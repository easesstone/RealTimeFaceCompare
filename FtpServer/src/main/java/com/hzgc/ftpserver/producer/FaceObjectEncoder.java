package com.hzgc.ftpserver.producer;

import com.hzgc.ftpserver.util.BeanUtils;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;


public class FaceObjectEncoder implements Encoder<FaceObject> {
    public FaceObjectEncoder(VerifiableProperties verifiableProperties) {
    }

    @Override
    public byte[] toBytes(FaceObject faceObject) {
        return BeanUtils.objectToBytes(faceObject);
    }
}

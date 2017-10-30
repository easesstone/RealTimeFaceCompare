package com.hzgc.jni;

import com.hzgc.jni.BeanUtils;
import com.hzgc.jni.FaceAttr;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;


public class FaceAttrEncoder implements Encoder<FaceAttr> {
    public FaceAttrEncoder(VerifiableProperties verifiableProperties) {
    }

    @Override
    public byte[] toBytes(FaceAttr faceAttr) {
        return BeanUtils.objectToBytes(faceAttr);
    }
}

package com.hzgc.jni;


import com.hzgc.jni.BeanUtils;
import com.hzgc.jni.FaceAttr;
import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

public class FaceAttrDecoder implements Decoder<FaceAttr> {
    public FaceAttrDecoder(VerifiableProperties verifiableProperties) {
    }
    @Override
    public FaceAttr fromBytes(byte[] bytes) {
        return BeanUtils.bytesToObject(bytes);
    }
}

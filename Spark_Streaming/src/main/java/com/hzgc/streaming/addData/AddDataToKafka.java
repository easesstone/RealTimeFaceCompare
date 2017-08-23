package com.hzgc.streaming.addData;

import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.*;
import java.util.Properties;

/**
 * Created by Administrator on 2017/8/1.
 */
public class AddDataToKafka implements Serializable {
    private final Producer<String, byte[]> producer;
    public static String TOPIC = "testPhoto";

    private AddDataToKafka() {
        Properties props = new Properties();
        // 此处配置的是kafka的broker地址:端口列表
        props.put("metadata.broker.list", "172.18.18.107:21005,172.18.18.108:21005,172.18.18.109:21005");
        //配置key的序列化类
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");
        //配置value的序列化类
        props.put("value.serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("request.required.acks", "-1");
        producer = new Producer<String, byte[]>(new ProducerConfig(props));
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n = 0;
            while ((n = fis.read(b)) != -1) {
                //  System.out.println(new String(b,0,n));
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static void main(String[] args) throws Exception {
        new AddDataToKafka().produce();
    }

    void produce() throws Exception {

//        String filePath = "C:\\Users\\Administrator\\Desktop\\人脸比对需求文档\\静态信息库图片\\彭聪-2017_8_11_17_32_913.jpg";
        String filePath="C:\\Users\\Administrator\\Desktop\\人脸比对需求文档\\静态信息库图片\\1.jpg";
        byte[] bt = File2byte(filePath);
        //算法初始化
        NativeFunction.init();
        for (int i = 2; i < 10; i++) {
            float[] feature = FaceFunction.featureExtract(bt);//17130NCY0HZ0001-T
            String feature2string = FaceFunction.floatArray2string(feature);
            //17130NCY0HZ0004-0_00000000000000_170523160015_0000004075_02
            producer.send(new KeyedMessage<String, byte[]>(TOPIC, "17130NCY0HZ000" + i + "-T_00000000000000_170523160015_0000004015_02", feature2string.getBytes("ISO-8859-1")));
            // producer.send(new KeyedMessage<String, byte[]>(TOPIC,"0003"+i+"-T_00000000000000_170523160015_0000004015_02"  ,feature2string.getBytes("ISO-8859-1")));
        }
        //销毁算法连接
        NativeFunction.destory();
    }


}

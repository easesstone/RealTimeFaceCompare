package com.hzgc.phoenix;


import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.LiteralExpression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode.Argument;
import org.apache.phoenix.parse.FunctionParseNode.BuiltInFunction;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PFloat;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.schema.types.PVarchar;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

@BuiltInFunction(name = FaceCompFunc.NAME,  args = {
        @Argument(allowedTypes = {PVarchar.class}),
        @Argument(allowedTypes = {PVarchar.class})})
public class FaceCompFunc extends ScalarFunction {
    Logger LOG = Logger.getLogger(FaceCompFunc.class);
    public static final String NAME = "FACECOMP";

    private String thePassStr = null;

    public FaceCompFunc(){}

    public FaceCompFunc(List<Expression> children) throws SQLException{
        super(children);
        init();
    }

    private void init() {
        LOG.info("start init............");
        Expression strToSearchExpression = getChildren().get(1);
        if (strToSearchExpression instanceof LiteralExpression) {
            Object strToSearchValue = ((LiteralExpression) strToSearchExpression).getValue();
            if (strToSearchValue != null) {
                this.thePassStr = strToSearchValue.toString();
            }
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
        Expression child = getChildren().get(0);


        if (!child.evaluate(tuple, ptr)) {
            return false;
        }

        float related;
        if (ptr.getLength() == 0) {
            related = 0f;
            ptr.set(PFloat.INSTANCE.toBytes(related));
            return true;
        }

        //Logic for Empty string search
        if (thePassStr == null || thePassStr.length() != 2048){
            related = 0f;
            ptr.set(PFloat.INSTANCE.toBytes(related));
            return true;
        }

        String sourceStr = (String) PVarchar.INSTANCE.toObject(ptr, getChildren().get(0).getSortOrder());

        related = featureCompare(thePassStr, sourceStr);

        ptr.set(PInteger.INSTANCE.toBytes(related));
        return true;
    }

    @Override
    public PDataType getDataType() {
        return PFloat.INSTANCE;
    }

    public static float featureCompare(String currentFeatureStr, String historyFeatureStr) {
        float[] currentFeature = string2floatArray(currentFeatureStr);
        float[] historyFeature = string2floatArray(historyFeatureStr);
        return featureCompare(currentFeature, historyFeature);
    }

    public static float featureCompare(float[] currentFeature, float[] historyFeature) {
        double similarityDegree = 0;
        double currentFeatureMultiple = 0;
        double historyFeatureMultiple = 0;
        for (int i = 0; i < currentFeature.length; i++) {
            similarityDegree = similarityDegree + currentFeature[i] * historyFeature[i];
            currentFeatureMultiple = currentFeatureMultiple + Math.pow(currentFeature[i], 2);
            historyFeatureMultiple = historyFeatureMultiple + Math.pow(historyFeature[i], 2);
        }

        double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);
        double actualValue = new BigDecimal((0.5 + (tempSim / 2)) * 100).
                setScale(2, BigDecimal.ROUND_HALF_UP).
                doubleValue();
        if (actualValue >= 100) {
            return 100;
        }
        return (float) actualValue;
    }

    public static float[] string2floatArray(String feature) {
        float[] floatFeature;
        if (null != feature && feature.length() > 0) {
            try {
                byte[] byteFeature = feature.getBytes("ISO-8859-1");
                floatFeature = new float[byteFeature.length / 4];
                byte[] buffer = new byte[4];
                int countByte = 0;
                int countFloat = 0;
                while (countByte < byteFeature.length && countFloat < floatFeature.length) {
                    buffer[0] = byteFeature[countByte];
                    buffer[1] = byteFeature[countByte + 1];
                    buffer[2] = byteFeature[countByte + 2];
                    buffer[3] = byteFeature[countByte + 3];
                    if (countByte % 4 == 0) {
                        floatFeature[countFloat] = byte2float(buffer, 0);
                    }
                    countByte = countByte + 4;
                    countFloat++;
                }
                return floatFeature;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static float byte2float(byte[] featureByte, int index) {
        int l;
        l = featureByte[index + 0];
        l &= 0xff;
        l |= ((long) featureByte[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) featureByte[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) featureByte[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

}

package com.hzgc.service.staticrepo;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.LiteralExpression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@FunctionParseNode.BuiltInFunction(name = FaceCompFunc.NAME,  args = {
        @FunctionParseNode.Argument(allowedTypes = {PFloatArray.class}),
        @FunctionParseNode.Argument(allowedTypes = {PVarchar.class})})
public class FaceCompFunc extends ScalarFunction {
    public static final String NAME = "FACECOMP";

    private List<float[]> thePassfeatures = new ArrayList<float[]>();

    public FaceCompFunc(){
    }

    public FaceCompFunc(List<Expression> children) throws SQLException {
        super(children);
        init();
    }

    private void init() {
        Expression strToSearchExpression = getChildren().get(1);
        if (strToSearchExpression instanceof LiteralExpression) {
            Object strToSearchValue = ((LiteralExpression) strToSearchExpression).getValue();
            if (strToSearchValue != null) {
                String featuresString = strToSearchValue.toString();
                String[] features = featuresString.split(",");
                for (String feature : features) {
                    thePassfeatures.add(string2floatArray(feature));
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
        Expression child = getChildren().get(0);


        if (!child.evaluate(tuple, ptr)) {
            return false;
        }

        float finalRelated;
        if (ptr.getLength() == 0) {
            finalRelated = 0f;
            ptr.set(PFloat.INSTANCE.toBytes(finalRelated));
            return true;
        }

        //Logic for Empty string search
        if (thePassfeatures.size() == 0){
            finalRelated = 0f;
            ptr.set(PFloat.INSTANCE.toBytes(finalRelated));
            return true;
        }

        PhoenixArray.PrimitiveFloatPhoenixArray primitiveFloatPhoenixArray =
                (PhoenixArray.PrimitiveFloatPhoenixArray) PFloatArray.INSTANCE.toObject(ptr, getChildren().get(0).getSortOrder());
        float[] sourceFeature = new float[0];
        try {
            sourceFeature = (float[]) primitiveFloatPhoenixArray.getArray();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        finalRelated = 0;
        for (float[] feature : thePassfeatures) {
            float currentRelated = featureCompare(feature, sourceFeature);
            if( currentRelated > finalRelated) {
                finalRelated = currentRelated;
            }
        }

        ptr.set(PFloat.INSTANCE.toBytes(finalRelated));
        return true;
    }


    @Override
    public PDataType getDataType() {
        return PFloat.INSTANCE;
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

    /**
     * 将特征值（String）转换为特征值（float[]）（内）（赵喆）
     *
     * @param feature 传入编码为UTF-8的String
     * @return 返回float[]类型的特征值
     */
    public static float[] string2floatArray(String feature) {
        if (feature != null && feature.length() > 0) {
            float[] featureFloat = new float[512];
            String[] strArr = feature.split(":");
            for (int i = 0; i < strArr.length; i++) {
                featureFloat[i] = Float.valueOf(strArr[i]);
            }
            return featureFloat;
        }
        return new float[0];
    }
}

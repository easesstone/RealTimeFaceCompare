package com.hzgc.udf.spark;

import java.math.BigDecimal;
import java.util.List;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.DoubleWritable;

public class UDFArrayCompare extends GenericUDF
{
    private DoubleWritable result;
    private float[] floatFeature = null;

    public ObjectInspector initialize(ObjectInspector[] arguments)
            throws UDFArgumentException
    {
        this.floatFeature = null;
        this.result = new DoubleWritable(0.0D);
        return PrimitiveObjectInspectorFactory.writableDoubleObjectInspector;
    }

    public Object evaluate(GenericUDF.DeferredObject[] arguments) throws HiveException {
        Object leftArray = arguments[0].get();
        Object rightArray = arguments[1].get();
        if (null == this.floatFeature)
            this.floatFeature = string2floatArray(leftArray.toString());
        else {
            try {
                List<Float> historyFeature = (List<Float>)rightArray;
                double ret = featureCompare(this.floatFeature, historyFeature);
                this.result.set(ret);
            } catch (Exception e) {
                this.result.set(0.0D);
            }
        }

        return this.result;
    }

    public String getDisplayString(String[] strings) {
        assert (strings.length == 2);
        return "array_compare(" + strings[0] + ", " + strings[1] + ")";
    }

    private static float[] string2floatArray(String feature) {
        if ((feature != null) && (feature.length() > 0)) {
            float[] featureFloat = new float[512];
            String[] strArr = feature.split(":");
            for (int i = 0; i < strArr.length; i++) {
                try {
                    featureFloat[i] = Float.valueOf(strArr[i]);
                } catch (Exception e) {
                    return new float[0];
                }
            }
            return featureFloat;
        }
        return new float[0];
    }

    private double featureCompare(float[] currentFeature, List<Float> historyFeature) {
        double similarityDegree = 0.0D;
        double currentFeatureMultiple = 0.0D;
        double historyFeatureMultiple = 0.0D;
        if ((currentFeature.length == 512) && (historyFeature.size() == 512)) {
            for (int i = 0; i < currentFeature.length; i++) {
                similarityDegree += currentFeature[i] * historyFeature.get(i);
                currentFeatureMultiple += Math.pow(currentFeature[i], 2.0D);
                historyFeatureMultiple += Math.pow(historyFeature.get(i), 2.0D);
            }
            double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);

            double actualValue = new BigDecimal((0.5D + tempSim / 2.0D) * 100.0D)
                    .setScale(2, 4)
                    .doubleValue();
            if (actualValue >= 100.0D) {
                return 100.0D;
            }
            return actualValue;
        }
        return 0.0D;
    }
}
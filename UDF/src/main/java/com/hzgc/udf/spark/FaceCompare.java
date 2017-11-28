package com.hzgc.udf.spark;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.DoubleWritable;

import java.math.BigDecimal;
import java.util.List;

public class FaceCompare extends GenericUDF {
    private DoubleWritable result;

    public ObjectInspector initialize(ObjectInspector[] arguments)
            throws UDFArgumentException {
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException("The function array_compare(array, array) takes exactly 2 arguments.");
        }

        if (!arguments[1].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(1, "\"array\" expected at function array_compare, but \"" + arguments[1]
                    .getTypeName() + "\" is found");
        }

        ObjectInspector leftValueOI = arguments[0];
        ListObjectInspector rightArrayOI = (ListObjectInspector) arguments[1];
        ObjectInspector rightArrayElementOI = rightArrayOI.getListElementObjectInspector();

        if (!ObjectInspectorUtils.compareSupported(leftValueOI)) {
            throw new UDFArgumentException("The function array_compare does not support comparison for \"" + leftValueOI
                    .getTypeName() + "\" types");
        }

        if (!ObjectInspectorUtils.compareSupported(rightArrayElementOI)) {
            throw new UDFArgumentException("The function array_compare does not support comparison for \"" + rightArrayElementOI
                    .getTypeName() + "\" types");
        }

        ObjectInspector rightArrayElementValue = ObjectInspectorFactory.getStandardListObjectInspector
                (PrimitiveObjectInspectorFactory.javaFloatObjectInspector);
        if (rightArrayElementValue != arguments[1]){
            result.set(0.0D);
        }
        this.result = new DoubleWritable(0.0D);
        return PrimitiveObjectInspectorFactory.writableDoubleObjectInspector;
    }

    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object leftArray = arguments[0].get();
        Object rightArray = arguments[1].get();
        float[] currentFeature = string2floatArray(leftArray.toString());
        List historyFeature = (List) rightArray;
        double ret = featureCompare(currentFeature, historyFeature);
        this.result.set(ret);
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
                    featureFloat[i] = Float.valueOf(strArr[i]).floatValue();
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
                similarityDegree += currentFeature[i] * ((Float) historyFeature.get(i)).floatValue();
                currentFeatureMultiple += Math.pow(currentFeature[i], 2.0D);
                historyFeatureMultiple += Math.pow(((Float) historyFeature.get(i)).floatValue(), 2.0D);
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
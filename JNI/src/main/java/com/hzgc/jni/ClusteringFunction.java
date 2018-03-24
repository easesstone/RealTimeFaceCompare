package com.hzgc.jni;

import java.io.Serializable;


public class ClusteringFunction implements Serializable {
    /**
     * permanent resident population calculate
     *
     * @param features features array
     * @param dataSize features count
     * @param threshold similarity threshold
     * @param appearCount people appear times count
     * @param fileName file name year-month.txt(2018-02.txt)
     * @param filePath absolute path of the results file
     * @return 0 or 1, 1 success, 0 fail
     */
    public static native int clusteringComputer(float[] features, int dataSize, double threshold, int appearCount, String fileName, String filePath);

    static {
        System.loadLibrary("resident");
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
    }
}

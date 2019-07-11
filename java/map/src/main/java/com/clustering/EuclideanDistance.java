package com.clustering;
import com.utils.Vector;

/** 计算两个向量之间的欧几里得距离 */
public class EuclideanDistance extends DistanceMetric {
    protected double calcDistance(Vector vector1, Vector vector2) {
        return vector1.getEuclideanDistance(vector2);
    }
}
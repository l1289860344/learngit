package com.clustering;

import com.utils.Vector;

/**
 * 抽象类:计算距离,距离越近越相似
 */
public abstract class DistanceMetric {
    /** 计算数据点data距离cluster质心的距离 */
    public double calcDistance(Data data, Cluster cluster) {
        return calcDistance(data.getVector(), cluster.getCentroid());
    }

    /**
     * 计算某一数据点data距离所有簇质心的最小值
     * @param Data,数据点
     * @param ClusterList,簇集合
     * @return distance,最小距离
     */
    public double calcDistance(Data data, ClusterList clusterList) {
        double distance = Double.MAX_VALUE;
        for (Cluster cluster : clusterList) {
            distance = Math.min(distance, calcDistance(data, cluster));
        }
        return distance;
    }

    /** 计算向量之间的距离 */
    protected abstract double calcDistance(Vector vector1, Vector vector2);
}
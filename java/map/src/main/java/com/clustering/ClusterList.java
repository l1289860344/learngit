package com.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
/** 多个簇操作的类  */
public class ClusterList implements Iterable<Cluster> {
    private final ArrayList<Cluster> clusters = new ArrayList<Cluster>();
    /** 添加一个簇 */
    public void add(Cluster cluster) {
        clusters.add(cluster);
    }
    /**
     * 迭代后需要将簇的数据全部移除
     */
    public void clear() {
        for (Cluster cluster : clusters) {
            cluster.clear();
        }
    }
    /**
     * 计算未分配的数据离质心的距离，确定一个离质心最远的一个点,这里采用的是欧几里得距离公式
     * @param DistanceMetric,距离
     * @param DataList,数据集合
     * @return Data,数据点
     */
    public Data findFurthestData(DistanceMetric distance, DataList dataList) {
        double furthestDistance = Double.MIN_VALUE;
        Data furthestData = null;
        for (Data data : dataList) {
            if (!data.isAllocated()) {
                //找最远距离
                double dataDistance = distance.calcDistance(data, this);
                if (dataDistance > furthestDistance) {
                    furthestDistance = dataDistance;
                    furthestData = data;
                }
            }
        }
        return furthestData;
    }
    /**
     * 寻找数据点data距离最近的簇
     *
     * @param DistanceMetric,距离
     * @param data,数据
     * @return Cluster,簇
     */
    public Cluster findNearestCluster(DistanceMetric distance, Data data) {
        Cluster nearestCluster = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Cluster cluster : clusters) {
            //计算距离
            double clusterDistance = distance.calcDistance(data, cluster);
            if (clusterDistance < nearestDistance) {
                nearestDistance = clusterDistance;
                nearestCluster = cluster;
            }
        }
        return nearestCluster;
    }

    @Override
    public Iterator<Cluster> iterator() {
        return clusters.iterator();
    }

    /**返回簇的数量 */
    public int size() {
        return clusters.size();
    }
    /**
     *数据点序号排序,然后排序cluster
     */
    private void sort() {
        for (Cluster cluster : this) {
            cluster.sort();
        }
        Collections.sort(clusters);
    }
    /**
     * 输出情况下展示结果
     */
    public String toString() {
        sort();
        StringBuilder sb = new StringBuilder();
        int clusterIndex = 0;
        for (Cluster cluster : clusters) {
            sb.append("Cluster ");
            sb.append(clusterIndex++);
            sb.append("\n");
            sb.append(cluster);
        }
        return sb.toString();
    }

    /**基于各维度的算术平均值更新每个簇质心 */
    public void updateCentroids() {
        for (Cluster cluster : clusters) {
            cluster.updateCentroid();
        }
    }

}

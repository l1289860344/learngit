package com.clustering;

import java.util.Random;

public class KMeansClusterer implements Clusterer {
    private static final Random RANDOM = new Random();
    private final int clusteringIterations;
    private final DistanceMetric distance;
    /**
     * Construct a Clusterer.
     * @param K-means聚类需要使用到的距离公式
     * @param k-means的迭代次数
     */
    public KMeansClusterer(DistanceMetric distance, int clusteringIterations) {
        this.distance = distance;
        this.clusteringIterations = clusteringIterations;
    }

    /** 计算所有未分配的数据点到质心的距离,基于最小原则给数据分配簇 */
    private void assignUnallocatedDataPoints(DataList dataList, ClusterList clusterList) {
        for (Data data : dataList) {
            if (!data.isAllocated()) {
                //寻找离data数据点最近的簇
                Cluster nearestCluster = clusterList.findNearestCluster(distance, data);
                //将数据data添加到该簇中
                nearestCluster.add(data);
            }
        }
    }
    /** 基于最远距离创建一个新的簇 */
    private Cluster createClusterBasedFurthestData(DataList dataList,ClusterList clusterList) {
        //找离该中心点最远的点
        Data furthestDocument = clusterList.findFurthestData(distance, dataList);
        //创建一个新的簇
        Cluster nextCluster = new Cluster(furthestDocument);
        return nextCluster;
    }

    /** 随机选择一个数据点作为质心 */
    private Cluster createClusterWithRandomlySelectedDataPoint(DataList dataList) {
        int rndDataIndex = RANDOM.nextInt(dataList.size()); // 随机取数据编号
        //将该编号对应的数据点进行分配,并将数据标记为已分配
        Cluster initialCluster = new Cluster(dataList.get(rndDataIndex));
        return initialCluster;
    }

    /** kmeans运行步骤*/
    public ClusterList runKMeansClustering(DataList dataList, int k) {
        ClusterList clusterList = new ClusterList();
        dataList.clearIsAllocated();  //清除数据分配
        //随机选择一个点，创建一个初始的簇
        clusterList.add(createClusterWithRandomlySelectedDataPoint(dataList));
        //如果簇的数量小于定义的簇的数量,则基于离质点最远的点,创建新的簇
        while (clusterList.size() < k) {
            clusterList.add(createClusterBasedFurthestData(dataList, clusterList));
        }
        //开始迭代
        for (int iter = 0; iter < clusteringIterations; iter++) {
            //基于质心和数据点的距离,分配没有分配的数据
            assignUnallocatedDataPoints(dataList, clusterList);
            //更新质心,取每个簇所有数据点的各维度的均值
            clusterList.updateCentroids();
            if (iter < clusteringIterations - 1) {
                //簇中的数据清空,进行重新迭代分配
                clusterList.clear();
            }
        }
        return clusterList;
    }
}
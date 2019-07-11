package com.clustering;

/**
 * 聚类接口
 */
public interface Clusterer {
    public ClusterList runKMeansClustering(DataList documentList, int k);
}

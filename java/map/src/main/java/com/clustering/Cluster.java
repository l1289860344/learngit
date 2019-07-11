package com.clustering;

import com.utils.Vector;
/** 单个簇操作的类 */
public class Cluster implements Comparable<Cluster> {
    private Vector centroid;
    private final DataList datas = new DataList();
    private final int numFeatures;

    /** 随机取一个数据点作为质心 */
    public Cluster(Data data) {
        add(data);
        centroid = new Vector(data.getVector());
        numFeatures = data.getNumFeatures();
    }
    /** 簇中添加数据,并将该数据设置为已分配 */
    public void add(Data data) {
        data.setIsAllocated();
        datas.add(data);
    }
    /** 将簇中的数据移除 */
    public void clear() {
        datas.clearIsAllocated();
        datas.clear();
    }
    /** 对簇中的数据进行排序. */
    @Override
    public int compareTo(Cluster cluster) {
        if (datas.isEmpty() || cluster.datas.isEmpty()) {
            return 0;
        }
        return datas.get(0).compareTo(cluster.datas.get(0));
    }
    /** 获取质心 */
    public Vector getCentroid() {
        return centroid;
    }
    /** 获取数据 */
    public DataList getDatas() {
        return datas;
    }

    /** 簇中数据的数量 */
    public int size() {
        return datas.size();
    }

    /** 基于数据点的id，排序 */
    public void sort() {
        datas.sort();
    }
    /** 需要展示的结果 */
    @Override
    public String toString() {
        return datas.toString();
    }

    /** 更新该 簇的质心 */
    public void updateCentroid() {
        centroid = new Vector(numFeatures);
        //该簇中的数据进行循环
        for (Data data : datas) {
            centroid = centroid.add(data.getVector());
        }
        centroid = centroid.divide(size());
    }
}

package com.clustering;

import com.utils.Vector;

/** 单个数据点表示的类 */
public class Data implements Comparable<Data> {
    private final String title;
    private final long id;
    private boolean allocated;
    private  Vector vector;
    private int numFeatures;

    public Data(long id, String title) {
        this.id = id;
        this.title = title;
    }
    /** 数据点清除分配 */
    public void clearIsAllocated() {
        allocated = false;
    }

    /** 继承,按照id排序输出 */
    @Override
    public int compareTo(Data data) {
        if (id > data.getId()) {
            return 1;
        } else if (id < data.getId()) {
            return -1;
        } else {
            return 0;
        }
    }

    /** 获取数据id */
    public long getId() {
        return id;
    }
    /**获取数据标题 */
    public String getTitle() {
        return title;
    }
    /** 获取向量唯独 */
    public int getNumFeatures() {
        return numFeatures;
    }

    /**
     * 获取数据向量
     */
    public Vector getVector() {
        return vector;
    }

    /** 确定数据是否被分配到簇 */
    public boolean isAllocated() {
        return allocated;
    }


    /** 标记该数据点已被分配 */
    public void setIsAllocated() {
        allocated = true;
    }
    /**
     * 设置向量和维度
     */
    public void setVector(Vector vector) {
        this.vector = vector;
        this.numFeatures = vector.size();
    }
    //默认输出
    public String toString() {
        return "Data: " + id + ", Title: " + title;
    }
}
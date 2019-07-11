package com.utils;

import java.util.Arrays;

/**
 * 向量计算
 */
public class Vector {
    private final double[] elements;

    /** 固定长度的向量构造 */
    public Vector(int size) {
        elements = new double[size];
    }

    /** 复制数组 */
    public Vector(Vector vector) {
        elements = Arrays.copyOf(vector.elements, vector.elements.length);
    }

    /** 向量的加和操作 */
    public Vector add(Vector operAddition) {
        Vector result = new Vector(size());
        for (int i = 0; i < elements.length; i++) {
            result.set(i, get(i) + operAddition.get(i));
        }
        return result;
    }

    /** 向量除以一个常数 */
    public Vector divide(double divisor) {
        Vector result = new Vector(size());
        for (int i = 0; i < elements.length; i++) {
            result.set(i, get(i) / divisor);
        }
        return result;
    }

    /** 获取向量的第i个值 */
    public double get(int i) {
        return elements[i];
    }
    /** 计算向量之间的欧几里得距离 */
    public double getEuclideanDistance(Vector vector) {
        double euclideanDistance = 0;
        for (int i = 0; i < elements.length; i++) {
            euclideanDistance += Math.pow(get(i) - vector.get(i), 2);;
        }
        return Math.sqrt(euclideanDistance);
    }
    /** 向量添加值 */
    public void set(int i, double value) {
        elements[i] = value;
    }

    /** 返回elements的长度 */
    public int size() {
        return elements.length;
    }
    @Override
    public String toString() {
        return Arrays.toString(elements);
    }
}
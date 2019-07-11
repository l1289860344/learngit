package com.clustering;

import java.io.File;
import java.io.IOException;

import com.clustering.ClusterList;
import com.clustering.Clusterer;
import com.clustering.DistanceMetric;
import com.clustering.DataList;
import com.clustering.EuclideanDistance;
import com.clustering.KMeansClusterer;
import com.utils.OutPutFile;

public class ClusterMain {
    private static final int iter = 500; //设置总迭代次数
    private static final int feature_number = 2; //设置特征维度
    private static final int k = 5;  //确定簇的数目

    public static void main(String[] args) throws IOException {
        //文档目录
        String fileinput = "D:\\code\\study\\map\\src\\main\\java\\com\\clustering\\d1.txt";
//        new File(fileinput);
        //读取文件,将其转化成向量形式
        DataList documentList = new DataList(fileinput, feature_number);
        //定义距离公式,这里使用欧几里得距离
        DistanceMetric distance = new EuclideanDistance();
        Clusterer clusterer = new KMeansClusterer(distance, iter);
        //聚类
        ClusterList clusterList = clusterer.runKMeansClustering(documentList, k);
        System.out.println(clusterList);
        //输出结果
        OutPutFile.outputClusterAndContent("D:\\code\\study\\map\\src\\main\\java\\com\\clustering" + k, clusterList);
    }
}
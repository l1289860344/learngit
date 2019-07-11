package com.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.utils.Vector;

/** 数据集合表示和操作的类 */
public class DataList implements Iterable<Data> {
    private final List<Data> datas = new ArrayList<Data>();
    /** 构造空的DataList */
    public DataList() {
    }
    public DataList(String input,int numFeatures) throws IOException {
        File f =   new File(input);
        FileInputStream fis = new FileInputStream(f);
        BufferedReader reader = new BufferedReader( new InputStreamReader(fis ,"utf-8"));
        String s = null;
        int i = 0;
        while ((s=reader.readLine())!=null) {
            //标题
            String arry[] =s.split("\t");
            //向量内容
            String[] vectorString = arry[1].split("\\s+");
            Vector vector = new Vector(numFeatures);
            for (int j = 0; j < vectorString.length; j++) {
                //向量添加值
                vector.set(j, Double.parseDouble(vectorString[j]));
            }
            //文档的名称
            String title =arry[0];
            //输入编号,对应的向量,对应的名称
            Data data = new Data(i, title);
            data.setVector(vector);
            datas.add(data);
            i++;
        }
        reader.close();

    }
    /** 添加数据至DataList */
    public void add(Data data) {
        datas.add(data);
    }

    /** 数据集合移除 */
    public void clear() {
        datas.clear();
    }

    /** 将所有数据点设置为未分配 */
    public void clearIsAllocated() {
        for (Data data : datas) {
            data.clearIsAllocated();
        }
    }

    /** 获取id为index的数据点 */
    public Data get(int index) {
        return datas.get(index);
    }
    /** 确定集合是否为空 */
    public boolean isEmpty() {
        return datas.isEmpty();
    }

    @Override
    public Iterator<Data> iterator() {
        return datas.iterator();
    }

    /** 数据的数量 */
    public int size() {
        return datas.size();
    }

    /** 集合排序 */
    public void sort() {
        Collections.sort(datas);
    }
    /** 需要展示的结果 */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Data data : datas) {
            sb.append("  ");
            //获取数据
            sb.append(data.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}

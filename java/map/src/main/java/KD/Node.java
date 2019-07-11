package KD;

/**
 * @ProjectName: map
 * @Package: KD
 * @ClassName: Node
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/16 7:07
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/16 7:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Node {
    //分割的维度
    int partitionDimention;
    //分割的值
    double partitionValue;
    //如果为非叶子节点，该属性为空
    //否则为数据
    double[] value;
    //是否为叶子
    boolean isLeaf = false;
    //左树
    Node left;
    //右树
    Node right;
    //每个维度的最小值
    double[] min;
    //每个维度的最大值
    double[] max;
}

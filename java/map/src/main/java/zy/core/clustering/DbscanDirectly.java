package zy.core.clustering;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.NullArgumentException;
import zy.StringUtils;
import zy.core.Point;
import zy.core.clustering.distance.LatLonUtil;
import zy.core.clustering.distance.LngLat;
import zy.core.commons.Instance;
import zy.core.distance.CosineDistance;
import zy.core.distance.DistanceMeasure;
import zy.core.json.ReadUtils;
import zy.core.txt_utils.TxtUtils;
import zy.util.kdtree.KDTree;

import java.text.SimpleDateFormat;
import java.util.*;


public class DbscanDirectly<T extends Clusterable> extends ClusteringAlgorithm<T> {
    private int totalPtNum = 0;
    private int noiseNum = 0;

    public List<Lnglat> getNoisePoint() {
        return this.noisePoint;
    }

    public void setNoisePoint(List<Lnglat> noisePoint) {
        this.noisePoint = noisePoint;
    }

    public List<Lnglat> getAroundPoint() {
        return this.aroundPoint;
    }

    public void setAroundPoint(List<Lnglat> aroundPoint) {
        this.aroundPoint = aroundPoint;
    }

    private List<Lnglat> aroundPoint = new ArrayList<Lnglat>();
    private List<Lnglat> noisePoint = new ArrayList<Lnglat>();
    /**
     * Maximum radius of the neighborhood to be considered.
     */
    private double eps;

    /**
     * Minimum number of points needed for a cluster.
     */
    private int minPts;

    /**
     * SInstanceatus of a point during the clustering process.
     */
    private enum InstanceStatus {
        /**
         * The point has is considered to be noise.
         */
        NOISE,
        /**
         * The point is already part of a cluster.
         */
        PART_OF_CLUSTER
    }

    private KDTree kdtree;

    /**
     * Creates a new instance of a DBSCANClusterer.
     * <p>
     * The euclidean distance will be used as default distance measure.
     *
     * @param eps    maximum radius of the neighborhood to be considered
     * @param minPts minimum number of points needed for a cluster
     * @throws NotPositiveException if {@code eps < 0.0} or {@code minPts < 0}
     */
    public DbscanDirectly(final double eps, final int minPts) {
        this(eps, minPts, new CosineDistance());
    }

    /**
     * Creates a new instance of a DBSCANClusterer.
     *
     * @param eps     maximum radius of the neighborhood to be considered
     * @param minPts  minimum number of points needed for a cluster
     * @param measure the distance measure to use
     * @throws NotPositiveException if {@code eps < 0.0} or {@code minPts < 0}
     */
    public DbscanDirectly(final double eps, final int minPts,
                          final DistanceMeasure measure) {
        super(measure);

        if (eps < 0.0d) {
            throw new IllegalArgumentException(String.valueOf(eps));
        }
        if (minPts < 0) {
            throw new IllegalArgumentException(String.valueOf(minPts));
        }
        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * Returns the maximum radius of the neighborhood to be considered.
     *
     * @return maximum radius of the neighborhood
     */
    public double getEps() {
        return eps;
    }

    /**
     * Returns the minimum number of points needed for a cluster.
     *
     * @return minimum number of points needed for a cluster
     */
    public int getMinPts() {
        return minPts;
    }

    public int unClusterPoint(final Collection<T> instances) {
        int count = 0;
        for (final T instance : instances) {

            if (instance.isPart() != true) {
                count = count + 1;
            }
        }
        return count;
    }

    /**
     * Performs DBSCAN cluster analysis.
     *
     * @param instances the points to cluster
     * @return the list of clusters
     * @throws NullArgumentException if the data points are null
     */
    @Override
    public List<Cluster<T>> cluster(final Collection<T> instances) {
        if (instances == null || instances.isEmpty())
            throw new IllegalArgumentException("Instances does not exists!");

        int dimension = 2;

        final List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
        int i = 0;
        for (final T instance : instances) {

            if (instance.isPart() == true) {
                continue;
            }

            final List<T> neighbors = getNeighborsByRadius(instance, instances);

            //比如设置30个点, 找到的邻居是包含自己的，所以要-1
            if (neighbors.size() - 1 >= minPts) {
                // DBSCAN does not care about center points
                final Cluster<T> cluster = new Cluster<T>();
//                System.out.println("********************开始添加新的聚合大小为：" + neighbors.size());
                Cluster<T> currentCluster = expandCluster(cluster, instance, neighbors, instances);
                clusters.add(currentCluster);
                this.totalPtNum += cluster.getInstances().size();
                cluster.setAroundP(new Point(instance.getFeatures()[0], instance.getFeatures()[1]));
                i++;
                System.out.println("--------------------------------------------------------");
                System.out.println("完成一个聚合  : " + i + "。: 聚合点是：" + cluster.getInstances().size() + ",　　" + "剩余点是:" + unClusterPoint(instances));

         /*       List<Lnglat> lnglatList= new ArrayList<Lnglat>();
                for (T instance2 : cluster.getInstances()) {
                    double[] d = new double[2];
                    Lnglat lnglat = new Lnglat();
                    d[0] = instance2.getFeatures()[0];
                    d[1] = instance2.getFeatures()[1];
                    lnglat.setLnglat(d);
                    lnglatList.add(lnglat);
                }
                System.out.println("var mails =" + JSON.toJSONString(lnglatList));
                System.out.println("选中的聚类点数是：" + cluster.getInstances().size());
                System.out.println("choose point var lon=" + cluster.getAroundP().getX() + "; var lat=       " + cluster.getAroundP().getY() + ";");*/

                //                System.out.println("添加新的聚合大小为 = " + clusters.size() + "         " + neighbors.size() + "           " + cluster.getInstances().size());
            } /*else {
                this.noiseNum += 1;
                double[] d = new double[2];
                d[0] = StringUtils.toCoordDouble(instance.getFeatures()[0]);
                d[1] = StringUtils.toCoordDouble(instance.getFeatures()[1]);
                this.noisePoint.add(new Lnglat(d));
                visited.put(instance, InstanceStatus.NOISE);
            }*/
        }
//        System.out.println("before return = " + clusters.size());
        return clusters;
    }

    /**
     * Expands the cluster to include density-reachable items.
     * 已经邻居点，不用再添加到聚类
     *
     * @param cluster   Cluster to expand
     * @param instance  Point to add to cluster
     * @param neighbors List of neighbors
     * @param points    the data set
     * @param visited   the set of already visited points
     * @return the expanded cluster
     */
    private Cluster<T> expandCluster(final Cluster<T> cluster,
                                     final T instance,
                                     final List<T> neighbors,
                                     final Collection<T> points) {
        cluster.addInstance(instance);
        instance.setPart(true);
        int whileCnt = 0;
        int addCnt = 0;
        Queue<T> seeds = new LinkedList<T>(neighbors);
        //中心点的邻居如果没有聚合，则被标记并聚合，如果此邻居没有标记，并且此邻居的邻居达到min，被加回队列

        for (T t : neighbors) {
            boolean pStatus = t.isPart();
            if (pStatus != true) {
                t.setPart(true);
                cluster.addInstance(t);
            }
        }
        return cluster;
    }


    private List<T> getNeighborsByRadius(final T point, final Collection<T> points) {
        List<T> oList = new ArrayList();

        for (T t : points) {
            if (t.isPart() == true) {
                continue;
            }
            LngLat s = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
            LngLat e = new LngLat(t.getFeatures()[0], t.getFeatures()[1]);

            double dis = LatLonUtil.getDistance(s, e);
            if (dis <= DbscanDirectly.DISTANT) {
                oList.add(t);
            }
        }

        return oList;
    }


    public List<Clusterable> initInstances() {
        List<Clusterable> instances = new LinkedList<Clusterable>();

        String path = ReadUtils.class.getClassLoader().getResource("10030.txt").getPath();
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List<Point> points = jsonarray.toJavaList(Point.class);

        for (Point p : points) {
            instances.add(new Instance(new double[]{p.getX(), p.getY()}, null));
        }
        System.out.println("初始化数据total: " + instances.size());
        return instances;
    }


    //要求聚合距离  以米为单位，中心点的邻居要达到MIN_POINT，包括中心点
    public static final double DISTANT = 500.0000001;
    public static final int MIN_POINT = 300;

    public static void main(String[] args) {
        Date s = new Date();
        System.out.println("开始运行: " + s.toString());
        DbscanDirectly<Clusterable> dbscan = new DbscanDirectly<Clusterable>(DbscanDirectly.DISTANT, DbscanDirectly.MIN_POINT);
        System.out.println("begin read -----------------------------------------");
        List<Clusterable> instances = dbscan.initInstances();

        List<Cluster<Clusterable>> clusters = dbscan.cluster(instances);
        System.out.println("end cluster -----------------------------------------");
        int clusterId = 0;

//        clusters = dbscan.getClusterCenter(clusters);

        for (Cluster<Clusterable> cluster : clusters) {
            clusterId++;

            //只输出点数合格的聚类
            if (cluster.getInstances().size() >= DbscanDirectly.MIN_POINT) {
                double[] d2 = new double[2];
                d2[0] = StringUtils.toCoordDouble(cluster.getAroundP().getX());
                d2[1] = StringUtils.toCoordDouble(cluster.getAroundP().getY());
                dbscan.aroundPoint.add(new Lnglat(d2));
                System.out.println("-----------------------------------");
                System.out.println("cluster id = " + clusterId + "      点数为:" + cluster.getInstances().size());
                System.out.println("中心点是 ：" + cluster.getAroundP().getX() + "       " + cluster.getAroundP().getY());
            }
        }

        List<Lnglat> lnglatList = new ArrayList<Lnglat>();

        if (clusters.size() > 0) {

            Cluster<Clusterable> c1 = clusters.get(0);
            for (Clusterable instance : c1.getInstances()) {
//            System.out.println(instance);
                double[] d = new double[2];
                Lnglat lnglat = new Lnglat();
                d[0] = instance.getFeatures()[0];
                d[1] = instance.getFeatures()[1];
                lnglat.setLnglat(d);
                lnglatList.add(lnglat);
            }

            System.out.println("var mails =" + JSON.toJSONString(lnglatList));
            System.out.println("选中的聚类点数是：" + c1.getInstances().size());
            System.out.println("choose point var lon=" + c1.getAroundP().getX() + "; var lat=       " + c1.getAroundP().getY() + ";");
            System.out.println("end read -----------------------------------------begin cluster size = " + instances.size());

            System.out.println("聚类中的点数为 ： " + dbscan.totalPtNum + " , noiseNum = " + dbscan.noiseNum + "        ,合计" + (dbscan.totalPtNum + dbscan.noiseNum));
            System.out.println("总聚合数:" + clusters.size());

            for (Clusterable instance : instances) {
                if (instance.isPart() != true) {
                    double[] d = new double[2];
                    d[0] = instance.getFeatures()[0];
                    d[1] = instance.getFeatures()[1];
                    Lnglat l = new Lnglat(d);
                    dbscan.getNoisePoint().add(l);
                }
            }
            String noiseStr = JSON.toJSONString(dbscan.getNoisePoint());
            String aroundStr = JSON.toJSONString(dbscan.getAroundPoint());
            TxtUtils.writeFile(noiseStr, "noisePoint.txt");
            TxtUtils.writeFile(aroundStr, "aroundPoint.txt");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        Date e = new Date();
        long between =  e.getTime() - s.getTime();
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long se = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        System.out.println("运行时间是：" + (day * 24 * 60 + hour * 60 + min) + "分" + se + "秒");


    }
}

/*
* 第一次结果：1.8W,运行时间是：2分28秒
* var mails =[{"lnglat":[120.202115,30.25536]},{"lnglat":[120.206519,30.253148]},{"lnglat":[120.201783,30.257167]},{"lnglat":[120.203827,30.258124]},{"lnglat":[120.20628,30.25302]},{"lnglat":[120.203932,30.256751]},{"lnglat":[120.203109,30.2523]},{"lnglat":[120.201995,30.256081]},{"lnglat":[120.206724,30.253627]},{"lnglat":[120.205001,30.255171]},{"lnglat":[120.200917,30.259417]},{"lnglat":[120.202547,30.25642]},{"lnglat":[120.20628,30.25302]},{"lnglat":[120.198767,30.255244]},{"lnglat":[120.206519,30.253148]},{"lnglat":[120.201558,30.253974]},{"lnglat":[120.20628,30.25302]},{"lnglat":[120.20597,30.25696]},{"lnglat":[120.200021,30.256574]},{"lnglat":[120.202663,30.258827]},{"lnglat":[120.202294,30.255114]},{"lnglat":[120.198381,30.255539]},{"lnglat":[120.205469,30.254362]},{"lnglat":[120.207038,30.256658]},{"lnglat":[120.203827,30.258124]},{"lnglat":[120.204911,30.254156]},{"lnglat":[120.202757,30.259166]},{"lnglat":[120.198865,30.253019]},{"lnglat":[120.202764,30.259023]},{"lnglat":[120.200988,30.254756]},{"lnglat":[120.199687,30.256266]},{"lnglat":[120.198466,30.258201]},{"lnglat":[120.199218,30.256518]},{"lnglat":[120.199006,30.25562]},{"lnglat":[120.205455,30.253756]}]
选中的聚类点数是：35
choose point var lon=120.202115; var lat=       30.25536;
end read -----------------------------------------begin cluster size = 18372
聚类中的点数为 ： 8332 , noiseNum = 0        ,合计8332
总聚合数:55
* */

/*存在问题
坐标相同的点，只有一个包括在聚合内

1  reset to yz begin to check radius 0614 0714"
 验证中心点与各点的距离是不是合法
 * 1 为什么要加随机数去重，不去OK
 * 2 两点间距离计算 OK
 * 3 如何测试：读取坐标文件OK
 * 4 大量坐标读入
 * 5 重心展示在地图上
 * 6 生成重心文件
 *not valid noise is = 120.16362800008918--30.24891600003619,  around point :120.16710600002797--30.248410000057437
 *
 *
 *  中心点(120.22193 == px6 && 30.252747 == py6) {
 * 上面这个中心点为什么最近的N个点中，会找到距离大于标准的点，但是噪音点中，此中心点可以找到N个标准距离内的点，可能是因为KD树最近的点，和标准距离的计算方法不一样
 * */


/*
 * 1 以中心点为邻的，至少要minPts个
 * 2 经过验证，重心与高德基本一致
 * */

/*
*
*     public List<Cluster<Clusterable>> getClusterCenter(List<Cluster<Clusterable>> clusters) {
        for (Cluster<Clusterable> cluster : clusters) {
            //点数达到 ，就计算中心点
            if (cluster.getInstances().size() >= Dbscan.MIN_POINT) {
                List<GeoCoordinate> geoCoordinateList = new ArrayList<GeoCoordinate>();

                for (Clusterable instance : cluster.getInstances()) {
                    GeoCoordinate g = new GeoCoordinate();
                    g.setLongitude(instance.getFeatures()[0]);
                    g.setLatitude(instance.getFeatures()[1]);
                    geoCoordinateList.add(g);
                }
                GeoCoordinate re = GetCenterPointFromListOfCoordinates.getCenterPoint(geoCoordinateList);
                Point centerP = new Point(0.0, 0.0);
                double[] d = new double[2];
                d[0] = re.getLongitude();
                d[1] = re.getLatitude();
                centerP.setLnglat(d);
                centerP.setX(re.getLongitude());
                centerP.setY(re.getLatitude());
                cluster.setCenterP(centerP);
//                System.out.println(re.getLongitude() + "   " + re.getLatitude());

                for (GeoCoordinate g : geoCoordinateList) {
                    g = null;
                }
                geoCoordinateList = null;
            }
        }

        return clusters;
    }
* */
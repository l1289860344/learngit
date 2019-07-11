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

import java.util.*;


public class DbscanBak<T extends Clusterable> extends ClusteringAlgorithm<T> {
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
    public DbscanBak(final double eps, final int minPts) {
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
    public DbscanBak(final double eps, final int minPts,
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

        int dimension = instances.iterator().next().getFeatures().length;
        kdtree = new KDTree(dimension);
        Random rand = new Random();
        final int RANDOM_LEVEL = 10;
        for (final T instance : instances) {
            double[] coord = instance.getFeatures();
            // 处理重复数据handle duplicate key condition
            for (int i = 0; i < dimension; i++) {
                coord[i] += rand.nextFloat() / Math.pow(10, RANDOM_LEVEL);
            }
            kdtree.insert(coord, instance);
        }

        final List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
        final Map<Clusterable, InstanceStatus> visited = new HashMap<Clusterable, InstanceStatus>();

        for (final T instance : instances) {
            if (visited.get(instance) != null) {
                continue;
            }
            double px = instance.getFeatures()[0];
            double py = instance.getFeatures()[1];
            if (120.221930 == (StringUtils.toCoordDouble(px)) && 30.252747 == StringUtils.toCoordDouble(py)) {
                System.out.println("come in");
            }

            final List<T> neighbors = getNeighborsByRadius(instance);
//            final List<T> neighbors = getNeighbors(instance, instances);
       /*     if(neighbors.size() <= 11){
                System.out.println("l t 11");
            }*/

            //比如设置30个点, 找到的邻居是包含自己的，所以要-1
            if (neighbors.size() - 1 >= minPts) {
                // DBSCAN does not care about center points
                final Cluster<T> cluster = new Cluster<T>();
//                System.out.println("********************开始添加新的聚合大小为：" + neighbors.size());
                clusters.add(expandCluster(cluster, instance, neighbors, instances, visited));
                this.totalPtNum += cluster.getInstances().size();
                cluster.setAroundP(new Point(instance.getFeatures()[0], instance.getFeatures()[1]));
//                System.out.println("添加新的聚合大小为 = " + clusters.size() + "         " + neighbors.size() + "           " + cluster.getInstances().size());
            } else {
                this.noiseNum += 1;
                double[] d = new double[2];
                d[0] = StringUtils.toCoordDouble(instance.getFeatures()[0]);
                d[1] = StringUtils.toCoordDouble(instance.getFeatures()[1]);
                this.noisePoint.add(new Lnglat(d));
                visited.put(instance, InstanceStatus.NOISE);
            }
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
                                     final Collection<T> points,
                                     final Map<Clusterable, InstanceStatus> visited) {
        cluster.addInstance(instance);
        visited.put(instance, InstanceStatus.PART_OF_CLUSTER);
        int whileCnt = 0;
        int addCnt = 0;
        Queue<T> seeds = new LinkedList<T>(neighbors);
        //中心点的邻居如果没有聚合，则被标记并聚合，如果此邻居没有标记，并且此邻居的邻居达到min，被加回队列
        while (!seeds.isEmpty()) {
            final T current = seeds.poll();
            InstanceStatus pStatus = visited.get(current);
            // only check non-visited points
        /*    if (pStatus == null) {
                //如果这个邻居点没有被标记过，不是聚合也不是噪音，要求这个邻居点的邻居数大于minP个点,如果小于，不加入队列
                final List<T> currentNeighbors = getNeighborsByRadius(current);
                if (currentNeighbors.size() - 1 >= minPts) {
                    seeds.add(current);
                }
            }*/
//如果这个邻居点没有聚合 ，则被标记并聚合
            if (pStatus != InstanceStatus.PART_OF_CLUSTER) {
                visited.put(current, InstanceStatus.PART_OF_CLUSTER);
                cluster.addInstance(current);
            }
        }

        return cluster;
    }

    public boolean findSamePoint(T point, List<T> tl) {
        double x = point.getFeatures()[0];
        double y = point.getFeatures()[1];
//        py.substring(0, 9)
        String strX = ("" + x);
        if (strX.length() > 9) {
            strX = strX.substring(0, 9);
        }

        String strY = ("" + x);
        if (strY.length() > 9) {
            strY = strY.substring(0, 9);
        }

        for (T t : tl) {
            String tx = ("" + t.getFeatures()[0]);
            if (tx.length() > 9) {
                tx = tx.substring(0, 9);
            }
            String ty = ("" + t.getFeatures()[1]);
            if (ty.length() > 9) {
                ty = ty.substring(0, 9);
            }
            if (strX.equals(tx) && strY.equals(ty)) {
                return true;
            }
        }
        return false;
    }


    //确认坐标半径是不是超过了要求
    public boolean checkRadius(final T point, T nearP, List<T> tl) {
        //已经存在相同的坐标在数组中
        if (findSamePoint(nearP, tl)) {
            return true;
        }

        double dx = point.getFeatures()[0] - nearP.getFeatures()[0];
        double dy = point.getFeatures()[1] - nearP.getFeatures()[1];

        LngLat s = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
        LngLat e = new LngLat(nearP.getFeatures()[0], nearP.getFeatures()[1]);

        double dis = LatLonUtil.getDistance(s, e);
        if (dis > DbscanBak.DISTANT)
            return false;
        else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> getNeighborsByRadius(final T point) {
        double step = eps;
        int dimension = point.getFeatures().length;
        double[] lowk = new double[dimension];
        double[] uppk = new double[dimension];

        LngLat disPoint = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
        double[] range = LatLonUtil.getAround(disPoint, DbscanBak.DISTANT);

       /* for (int i = 0; i < dimension; i++) {
            lowk[i] = point.getFeatures()[i] - step;
            uppk[i] = point.getFeatures()[i] + step;
        }*/
        //先横下，再横上，再纵下，再纵上
        lowk[0] = range[1];
        uppk[0] = range[3];
        lowk[1] = range[0];
        uppk[1] = range[2];

        List<T> tl = new ArrayList<T>();
        //确保点在指定的范围内
        Object[] nearest = kdtree.range(lowk, uppk);
        for (Object o : nearest) {
            T t = (T) o;
            if (checkRadius(point, t, tl) == true) {
                tl.add(t);
            }

        }
        return tl;
    }

    /**
     * Returns a list of density-reachable neighbors of a {@code point}.
     *
     * @param point  the point to look for
     * @param points possible neighbors
     * @return the List of neighbors
     */
    @SuppressWarnings("unchecked")
    private List<T> getNeighbors(final T point, final Collection<T> points) {
        double step = eps;
        int dimension = point.getFeatures().length;
        double[] lowk = new double[dimension];
        double[] uppk = new double[dimension];

        LngLat disPoint = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
        //求出正方形坐标
        double[] range = LatLonUtil.getAround(disPoint, DbscanBak.DISTANT);

        //先横下，再横上，再纵下，再纵上
        lowk[0] = range[1];
        uppk[0] = range[3];
        lowk[1] = range[0];
        uppk[1] = range[2];


        //确保点在指定的范围内
//        kdtree.nearest(point.getFeatures(), 10);
        Object[] nearest = kdtree.range(lowk, uppk);
        //	public Object[] nearestByDistance(double[] key, int n, double dis) {
        List<T> tl = new ArrayList<T>();
//        Object[] nearest = kdtree.nearestByDistance(point.getFeatures(), points.size(), Dbscan.DISTANT);
//        Object[] nearest = kdtree.nearest(point.getFeatures(), 10);
        for (Object o : nearest) {
            T t = (T) o;
            tl.add(t);
        }

//		return (List<T>) Arrays.asList(nearest);
        return tl;
    }


    public List<Clusterable> initInstances() {
        List<Clusterable> instances = new LinkedList<Clusterable>();

        String path = ReadUtils.class.getClassLoader().getResource("10030.json").getPath();
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List<Point> points = jsonarray.toJavaList(Point.class);

        for (Point p : points) {
            instances.add(new Instance(new double[]{p.getX(), p.getY()}, null));
        }

        return instances;
    }


    //要求聚合距离  以米为单位，中心点的邻居要达到MIN_POINT，包括中心点
    public static final double DISTANT = 500.0000001;
    public static final int MIN_POINT = 30;

    public static void main(String[] args) {
        DbscanBak<Clusterable> dbscan = new DbscanBak<Clusterable>(DbscanBak.DISTANT, DbscanBak.MIN_POINT);
        System.out.println("begin read -----------------------------------------");
        List<Clusterable> instances = dbscan.initInstances();

        List<Cluster<Clusterable>> clusters = dbscan.cluster(instances);
        System.out.println("end cluster -----------------------------------------");
        int clusterId = 0;

//        clusters = dbscan.getClusterCenter(clusters);

        for (Cluster<Clusterable> cluster : clusters) {
            clusterId++;

            //只输出点数合格的聚类
            if (cluster.getInstances().size() >= DbscanBak.MIN_POINT) {
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

        String noiseStr = JSON.toJSONString(dbscan.getNoisePoint());
        String aroundStr = JSON.toJSONString(dbscan.getAroundPoint());
        TxtUtils.writeFile(noiseStr, "noisePoint.txt");
        TxtUtils.writeFile(aroundStr, "aroundPoint.txt");
    }
}
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
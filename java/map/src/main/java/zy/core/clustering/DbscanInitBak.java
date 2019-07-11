package zy.core.clustering;

import org.apache.commons.lang.NullArgumentException;
import zy.core.Point;
import zy.core.clustering.distance.LngLat;
import zy.core.commons.Instance;
import zy.core.distance.CosineDistance;
import zy.core.distance.DistanceMeasure;
import zy.util.kdtree.KDTree;

import java.util.*;

public class DbscanInitBak<T extends Clusterable> extends ClusteringAlgorithm<T> {

    /**
     * Maximum radius of the neighborhood to be considered.
     */
    private double eps;

    /**
     * Minimum number of points needed for a cluster.
     */
    private int minPts;

    /**
     * Status of a point during the clustering process.
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
    public DbscanInitBak(final double eps, final int minPts) {
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
    public DbscanInitBak(final double eps, final int minPts,
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
    public List<Cluster<T>> cluster(Collection<T> instances) {
        if (instances == null || instances.isEmpty())
            throw new IllegalArgumentException("Instances does not exists!");

        int dimension = instances.iterator().next().getFeatures().length;
        kdtree = new KDTree(dimension);
        Random rand = new Random();
        final int RANDOM_LEVEL = 10;
        for (final T instance : instances) {
            double[] coord = instance.getFeatures();
            // handle duplicate key condition
            for (int i = 0; i < dimension; i++) {
                coord[i] += rand.nextFloat() / Math.pow(10, RANDOM_LEVEL);
                ;
            }
            kdtree.insert(coord, instance);
        }

        final List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
        final Map<Clusterable, InstanceStatus> visited = new HashMap<Clusterable, InstanceStatus>();

  /*      for (Object o : instances.toArray()) {
            T t = (T) o;
            System.out.println("x= " + t.getFeatures()[0] + ", y =" + t.getFeatures()[1]);
        }*/

//        for (final T instance : instances) {
        for (Object o : instances.toArray()) {
            T instance = (T) o;
//            System.out.println("开始循环点：x= " + instance.getFeatures()[0] + ", y =" + instance.getFeatures()[1]);
            if (visited.get(instance) != null) {
                continue;
            }
            double x = instance.getFeatures()[0];
            double y = instance.getFeatures()[1];
            /*
             * 调试的问题
             * 1 near point有没有包括中心点
             * 2 为什么只找到一个聚合
             * 3 分析range            *
             * */
     /*       if (x > 4 && x < 5 && y > 4 && y < 5) {
                System.out.println("开始调试x=" + instance.getFeatures()[0] + ", y=" +  instance.getFeatures()[1]);
                System.out.println("开始调试2");
            }*/

//            final List<T> neighbors = getNeighbors(instance);
            final List<T> neighbors = getNeighborsByRadius(instance);
            if (neighbors.size() - 1 >= minPts) {
                // DBSCAN does not care about center points
                final Cluster<T> cluster = new Cluster<T>();
                cluster.setAroundP((new Point(instance.getFeatures()[0], instance.getFeatures()[1])));
                cluster.setNearCnt(neighbors.size());
                clusters.add(expandCluster(cluster, instance, neighbors, instances, visited));

                System.out.println("----------------------------");
                System.out.println("选中的中心点是：" + cluster.getAroundP().toString());
                System.out.println("此中心点邻居数为：" + cluster.getNearCnt());
                for (Clusterable instance1 : cluster.getInstances()) {
                    System.out.println(instance1);
                }

            } else {
                visited.put(instance, InstanceStatus.NOISE);
            }
        }

        return clusters;
    }

    /**
     * Expands the cluster to include density-reachable items.
     *
     * @param cluster   Cluster to expand
     * @param instance  Point to add to cluster
     * @param neighbors List of neighborsD
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

        Queue<T> seeds = new LinkedList<T>(neighbors);
        while (!seeds.isEmpty()) {
            final T current = seeds.poll();
            InstanceStatus pStatus = visited.get(current);
            // only check non-visited points
            if (pStatus == null) {
                final List<T> currentNeighbors = getNeighbors(current);
                if (currentNeighbors.size() - 1 >= minPts) {
                    seeds.add(current);
                }
            }

            if (pStatus != InstanceStatus.PART_OF_CLUSTER) {
                visited.put(current, InstanceStatus.PART_OF_CLUSTER);
                cluster.addInstance(current);
            }
        }

        return cluster;
    }


    @SuppressWarnings("unchecked")
    private List<T> getNeighborsByRadius(final T point) {
        List<T> oList = new ArrayList();
        boolean inDistant = true;
        //确保点在指定的范围内
        Object[] ol = null;
        if (kdtree.getM_count() == 0) {
            inDistant = false;
            return oList;
        } else {
            //先批量找
            ol = kdtree.nearest(point.getFeatures(), DbscanInitBak.MIN_POINT);
        }

//        T[] nearestPl = (T[]) ol;
        T nearestP = null;
        for (Object o : ol) {
            nearestP = (T) o;
            LngLat s = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
            LngLat e = new LngLat(nearestP.getFeatures()[0], nearestP.getFeatures()[1]);
            double dis = Math.sqrt((s.latitude - e.latitude) * (s.latitude - e.latitude) + (s.longitude - e.longitude) * (s.longitude - e.longitude));
            if (dis > DbscanInitBak.DISTANT) {
                System.out.println("此点无法构成聚类");
                return oList;
//                oList.add(nearestP);
                //删除结点
//                kdtree.delete(((T) o).getFeatures());
            }
        }
        //确认第一步可以聚类
        for (Object o : ol) {
            nearestP = (T) o;
            oList.add(nearestP);
            kdtree.delete(((T) o).getFeatures());
        }

        //再一个一个找
        while (inDistant) {
            Object singleO = null;
            if (kdtree.getM_count() == 0) {
                inDistant = false;
                return oList;
            } else {
                singleO = kdtree.nearest(point.getFeatures());
                nearestP = (T) singleO;
                LngLat s = new LngLat(point.getFeatures()[0], point.getFeatures()[1]);
                LngLat e = new LngLat(nearestP.getFeatures()[0], nearestP.getFeatures()[1]);
                double dis = Math.sqrt((s.latitude - e.latitude) * (s.latitude - e.latitude) + (s.longitude - e.longitude) * (s.longitude - e.longitude));
                if (dis > DbscanInitBak.DISTANT) {
                    return oList;
                } else {
                    oList.add(nearestP);
                    kdtree.delete(((T) singleO).getFeatures());
                }
            }
        }
        return oList;
    }


    @SuppressWarnings("unchecked")
    private List<T> getNeighbors(final T point) {
        double step = eps;
        int dimension = point.getFeatures().length;
        double[] lowk = new double[dimension];
        double[] uppk = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            lowk[i] = point.getFeatures()[i] - step;
            uppk[i] = point.getFeatures()[i] + step;
        }

        Object[] nearest = kdtree.range(lowk, uppk);


        List<T> tl = new ArrayList<T>();
        for (Object o : nearest) {
            T t = (T) o;
            tl.add(t);
        }

        return tl;
    }


    public void showCollection(Collection<T> instances) {
        for (Object o : instances.toArray()) {
            T t = (T) o;
            System.out.println("x= " + t.getFeatures()[0] + ", y =" + t.getFeatures()[1]);
        }

    }

    private int totalPtNum = 0;
    private int noiseNum = 0;
    public static final double DISTANT = 1.0000000001;
    public static final int MIN_POINT = 2;

    public static void main(String[] args) {
        DbscanInitBak<Clusterable> dbscan = new DbscanInitBak<Clusterable>(DbscanInitBak.DISTANT, DbscanInitBak.MIN_POINT);
        List<Clusterable> instances = new LinkedList<Clusterable>();
        instances.add(new Instance(new double[]{4.0, 4.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{2.0, 2.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{3.0, 2.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{2.0, 3.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{3.0, 3.0}, new Object[]{1, 2, 3}));


        instances.add(new Instance(new double[]{5.0, 4.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{4.0, 5.0}, new Object[]{1, 2, 3}));
        instances.add(new Instance(new double[]{5.0, 5.0}, new Object[]{1, 2, 3}));

        List<Cluster<Clusterable>> clusters = dbscan.cluster(instances);
        int clusterId = 0;
        System.out.println("一共聚合数：" + clusters.size());

//        dbscan.showCollection(instances);
       /* for (Cluster<Clusterable> cluster : clusters) {
            System.out.println("----------------------------");
            System.out.println(clusterId++);
            System.out.println("选中的中心点是：" + cluster.getAroundP().toString());
            System.out.println("此中心点邻居数为：" + cluster.getNearCnt());
            for (Clusterable instance : cluster.getInstances()) {
                System.out.println(instance);
            }
        }*/
    }
}
/*
 * 需要重新确认的问题
 * 1 选定的中心点，是不是包涵在cluster中的
 * 2 确认半径查找，是不是准确的
 *
 * 下面的结果需要着重分析
 * 一共聚合数：1
选中的中心点是：Point{, x=4.0000009296284915, y=4.000000764915526}
此中心点邻居数为：4
4.0000009296284915 4.000000764915526 :
4.000000804402172 5.000000110231876 :
5.0000003930247425 4.000000320332825 :
5.000000383840621 5.000000222479284 :
 *
 *
 * */
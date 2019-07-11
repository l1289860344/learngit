package zy.core.clustering;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import KD.KdTree;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import org.apache.commons.lang.NullArgumentException;

import zy.StringUtils;
import zy.core.Point;
import zy.core.clustering.distance.LatLonUtil;
import zy.core.clustering.distance.LngLat;
import zy.core.commons.Instance;
import zy.core.distance.CosineDistance;
import zy.core.distance.DistanceMeasure;
import zy.core.json.ReadUtils;
import zy.core.txt_utils.PropertiesUtil;
import zy.core.txt_utils.TxtUtils;
import zy.util.kdtree.KDTree;


public class Dbscan<T extends Clusterable> extends ClusteringAlgorithm<T> {
    private int totalPtNum = 0;
    private int noiseNum = 0;
    //确认坐标点是不是已经被访问过
    private Map<Point2D, InstanceStatus> visited = new HashMap<Point2D, InstanceStatus>();

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

    private KdTree kdtree;

    public Dbscan(final double eps, final int minPts) {
        this(eps, minPts, new CosineDistance());
    }


    public Dbscan(final double eps, final int minPts,
                  final DistanceMeasure measure) {
        super(measure);
        this.DISTANT = eps;
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
        kdtree = new KdTree();
        Random rand = new Random();
        final int RANDOM_LEVEL = 10;
        for (final T instance : instances) {
            double[] coord = instance.getFeatures();
            // 处理重复数据handle duplicate key condition
            for (int i = 0; i < dimension; i++) {
                coord[i] += rand.nextFloat() / Math.pow(10, RANDOM_LEVEL);
            }
            Point2D p = new Point2D(coord[0], coord[1]);
            kdtree.insert(p);
        }
        System.out.println("完成kd树生成！！！！！！");
        final List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
//        final Map<Point2D, InstanceStatus> visited = new HashMap<Point2D, InstanceStatus>();
        int printCnt = 0;
        for (final T instance : instances) {
            printCnt++;
            //System.out.print("no. = " + printCnt + ",    time =" + this.getPastTime() + "               ");
            Point2D p3 = new Point2D(instance.getFeatures()[0], instance.getFeatures()[1]);
            if (visited.get(p3) != null) {
                continue;
            }
            double px = instance.getFeatures()[0];
            double py = instance.getFeatures()[1];
//            if (120.221930 == (StringUtils.toCoordDouble(px)) && 30.252747 == StringUtils.toCoordDouble(py)) {
//                System.out.println("come in");
//            }
            Point2D p = new Point2D(instance.getFeatures()[0], instance.getFeatures()[1]);
            final List<Point2D> neighbors = getNeighborsByRadius(p);
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
                System.out.println("当前已经聚合个数 = " + clusters.size() + "，         找到邻居个数：" + neighbors.size() + " ，本次聚合大小          " + cluster.getInstances().size() + ", 已经visited = " + visited.size()
                        + ",  总实例是: " + instances.size());
            } else {
              // System.out.println("-无法聚合-");
            } /*else {
                this.noiseNum += 1;
                double[] d = new double[2];
                d[0] = StringUtils.toCoordDouble(instance.getFeatures()[0]);
                d[1] = StringUtils.toCoordDouble(instance.getFeatures()[1]);
                this.noisePoint.add(new Lnglat(d));
                Point2D p2 = new Point2D(instance.getFeatures()[0], instance.getFeatures()[1]);
                visited.put(p2, InstanceStatus.NOISE);
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
                                     final List<Point2D> neighbors,
                                     final Collection<T> points,
                                     final Map<Point2D, InstanceStatus> visited) {

        cluster.addInstance(instance);
        Point2D pCenter = new Point2D(instance.getFeatures()[0], instance.getFeatures()[1]);
        visited.put(pCenter, InstanceStatus.PART_OF_CLUSTER);
        int whileCnt = 0;
        int addCnt = 0;
        Queue<Point2D> seeds = new LinkedList<Point2D>(neighbors);
        //中心点的邻居如果没有聚合，则被标记并聚合，如果此邻居没有标记，并且此邻居的邻居达到min，被加回队列
        while (!seeds.isEmpty()) {
            Point2D current = seeds.poll();

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
                double[] d = new double[2];
                d[0] = current.x();
                d[1] = current.y();
                T ins = (T) new Instance(d, null);
                cluster.addInstance(ins);
            }
        }

        return cluster;
    }

    public boolean findSamePoint(Point2D point, List<Point2D> tl) {
        double x = point.x();
        double y = point.y();
//        py.substring(0, 9)
        String strX = ("" + x);
        if (strX.length() > 9) {
            strX = strX.substring(0, 9);
        }

        String strY = ("" + x);
        if (strY.length() > 9) {
            strY = strY.substring(0, 9);
        }

        for (Point2D t : tl) {
            String tx = ("" + t.x());
            if (tx.length() > 9) {
                tx = tx.substring(0, 9);
            }
            String ty = ("" + t.y());
            if (ty.length() > 9) {
                ty = ty.substring(0, 9);
            }
            if (strX.equals(tx) && strY.equals(ty)) {
                return true;
            }
        }
        return false;
    }

    //确认此坐标是不是已经被访问过
    public boolean isVisited(Point2D p) {
        InstanceStatus s = visited.get(p);
        if (s == InstanceStatus.PART_OF_CLUSTER)
            return true;
        else
            return false;
    }

    //确认坐标半径是不是超过了要求
    public boolean checkRadius(Point2D point, Point2D nearP, List<Point2D> tl,RectHV rect) {
        if (isVisited(nearP) == true) {
            return false;
        }
        // 在内范围内不存在相同
        if(rect.contains(nearP) ){
            return true;
        }
        //已经存在相同的坐标在数组中
       /* if (findSamePoint(nearP, tl)) {
            return true;
        }*/
        double dx = point.x() - nearP.x();
        double dy = point.y() - nearP.y();

        LngLat s = new LngLat(point.x(), point.y());
        LngLat e = new LngLat(nearP.x(), nearP.y());

        double dis = LatLonUtil.getDistance(s, e);
        if (dis > this.DISTANT)
            return false;
        else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Point2D> getNeighborsByRadius(final Point2D point) {
//        double step = eps;
        int pos = 0;
        int dimension = 2;
        double[] lowk = new double[dimension];
        double[] uppk = new double[dimension];

        LngLat disPoint = new LngLat(point.x(), point.y());
        double[] range = LatLonUtil.getAround(disPoint, this.DISTANT);
       /* for (int i = 0; i < dimension; i++) {
            lowk[i] = point.getFeatures()[i] - step;
            uppk[i] = point.getFeatures()[i] + step;
        }*/
        //先横下，再横上，再纵下，再纵上
        lowk[0] = range[1];
        uppk[0] = range[3];
        lowk[1] = range[0];
        uppk[1] = range[2];


        List<Point2D> tl = new ArrayList<Point2D>();
        //确保点在指定的范围内   return new double[]{minLat, minLng, maxLat, maxLng};
//        RectHV rect = new RectHV(range[0], range[1], range[2], range[3]);
        RectHV rect = new RectHV(range[1], range[0], range[3], range[2]);
        Iterable<Point2D> it = kdtree.range(rect);

        double[] range2 = LatLonUtil.getAround(disPoint, this.DISTANT/100);
        RectHV rect2 = new RectHV(range2[1], range2[0], range2[3], range2[2]);

        int sum = 0;
        Iterator pi = it.iterator();
        while (pi.hasNext()) {
            sum = sum + 1;
            pi.next();
        }
        if (sum >= 20000) {
            System.out.println("本次找到的无过滤邻居个数是：" + sum);
        }

        if (sum < this.MIN_POINT) {
            return tl;
        }
        for (Point2D p : it) {
            pos++;
            if (sum >= 20000) {
                if (pos % 50 == 0) {
                    System.out.print("-" + pos + "-");
                }
            }

            if (checkRadius(point, p, tl,rect2) == true) {
                tl.add(p);
            }
        }
     /*   Object[] nearest = null;
        for (Object o : nearest) {
            T t = (T) o;
            if (checkRadius(point, t, tl) == true) {
                tl.add(t);
            }

        }*/
        return tl;
    }


    public List<Clusterable> initInstances(String cityNo) {
        String[] fileList = new String[1001];
        for (int i = 1; i <= 1000; i++) {
            fileList[i] = "" + i;
        }

       // System.out.println("读取文件数: " + fileList.length);
        List<Clusterable> instances = new LinkedList<Clusterable>();
//        System.out.println(System.getProperty("user.dir"));
        for (String fileName : fileList) {
//            String path = ReadUtils.class.getClassLoader().getResource(fileName).getPath();
            try {
                fileName = System.getProperty("user.dir") + File.separator + "data" + File.separator + cityNo + File.separator + fileName;
                String s = ReadUtils.readJsonFile(fileName);
                JSONArray jsonarray = JSONArray.parseArray(s);
                List<Point> points = jsonarray.toJavaList(Point.class);
                System.out.println(cityNo+" 开始读取文件:" + fileName+" 长度"+points.size());
                for (Point p : points) {
                    instances.add(new Instance(new double[]{p.getX(), p.getY()}, null));
                }
            } catch (Exception e) {
                //System.out.println("-文件X-");
                continue;
            }


        }

        System.out.println(cityNo+"读取总记录数：" + instances.size() + "，读取文件数: " + fileList.length);

        return instances;
    }

    //    private List<Lnglat> noisePoint = new ArrayList<Lnglat>();
    public List<Lnglat> getNoisePoint(List<Clusterable> instances) {
        Set<Point2D> set = visited.keySet();
        for (Clusterable c : instances) {
            Point2D p = new Point2D(c.getFeatures()[0], c.getFeatures()[1]);
            if (set.contains(p) == false) {
                double[] d = new double[2];
                d[0] = p.x();
                d[1] = p.y();
                Lnglat l = new Lnglat(d);
                this.noisePoint.add(l);
            }
        }

        return this.noisePoint;
    }

    public String getPastTime() {
        this.e = new Date();
        long between = this.e.getTime() - this.s.getTime();
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long se = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return ("" + (day * 24 * 60 + hour * 60 + min) + "分" + se + "秒");
    }

    //要求聚合距离  以米为单位，中心点的邻居要达到MIN_POINT，包括中心点
    public double DISTANT;
    public int MIN_POINT;
    /*
    *  public static final double DISTANT = 300.0000001;
    public static final int MIN_POINT = 200;
    * */
    public Date s = new Date();
    public Date e = new Date();

//        private String[] fileList = {"noisePoint.json"};
//    private String[] fileList = {"1001", "1002", "1003", "1004", "1005", "1006", "1007", "1008", "1009", "10010"};

//    private String[] fileList = {"test"};


 /*   private String[] fileList = {"1001", "10010", "10012", "10013", "10014", "10016", "10017", "10018", "10019",
            "1002", "10020", "10021", "10022", "10023", "10024", "10025", "10026", "10027", "10028", "10029",
            "1003", "10030", "10031", "10033", "10036", "10037", "10039", "1004", "10040", "10042", "10046", "10049",
            "1005", "10057", "10058", "1006", "1007", "1008", "1009"};*/

    public List<String> cityList;

    public void initMap(SysProperty sys) {
        String[] fileList = {"test"};
        this.cityList = sys.getCityList();
    }


    public static String pushdata(String cityNo,SysProperty sys) {
        StringBuffer notesb=new StringBuffer("");
        Dbscan<Clusterable> dbscan = new Dbscan<Clusterable>(sys.getRadius(), sys.getMinMailCount());
        System.out.println(cityNo+" 开始处理" + cityNo);
        dbscan.s = new Date();
        System.out.println(cityNo+" 开始运行: " + dbscan.s.toString());
        System.out.println("begin read -----------------------------------------");
        List<Clusterable> instances = dbscan.initInstances(cityNo);
        List<Cluster<Clusterable>> clusters = dbscan.cluster(instances);

        System.out.println("end cluster -----------------------------------------");
        int clusterId = 0;
        List<ResultPoint> rstPList = new ArrayList<ResultPoint>();
        for (Cluster<Clusterable> cluster : clusters) {
            clusterId++;

            //只输出点数合格的聚类
            if (cluster.getInstances().size() >= dbscan.MIN_POINT) {
                double[] d2 = new double[2];
                d2[0] = StringUtils.toCoordDouble(cluster.getAroundP().getX());
                d2[1] = StringUtils.toCoordDouble(cluster.getAroundP().getY());
                dbscan.aroundPoint.add(new Lnglat(d2));
                System.out.println("----------------"+cityNo+"-------------------");
                System.out.println("cluster id = " + clusterId + "      点数为:" + cluster.getInstances().size());
                System.out.println("中心点是 ：" + cluster.getAroundP().getX() + "       " + cluster.getAroundP().getY());
                ResultPoint rstP = new ResultPoint();
                rstP.setCount(cluster.getInstances().size());
                rstP.setLnglat(new double[]{cluster.getAroundP().getX(), cluster.getAroundP().getY()});
                rstPList.add(rstP);
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
        System.out.println("测试文本中心点是 ：" + c1.getAroundP().getX() + "," + c1.getAroundP().getY());
        String ClusterPath=System.getProperty("user.dir") + File.separator + "result" + File.separator + cityNo + "-ClusterPath.json";
        TxtUtils.writeFile(JSON.toJSONString(lnglatList), ClusterPath);

        //System.out.println("var mails =" + JSON.toJSONString(lnglatList));
       // System.out.println(cityNo+"选中的聚类点数是：" + c1.getInstances().size());
        //System.out.println("choose point var lon=" + c1.getAroundP().getX() + "; var lat= " + c1.getAroundP().getY() + ";");
       // System.out.println("end read -----------------------------------------begin cluster size = " + instances.size());
        String noiseStr = JSON.toJSONString(dbscan.getNoisePoint(instances));
        int noiseNm = dbscan.noisePoint.size();
        System.out.println("——————————————————");
        System.out.println("城市编号 ： " + cityNo);
        System.out.println("聚类中的点数为 ： " + dbscan.totalPtNum + " , noiseNum = " + noiseNm + "        ,合计" + (dbscan.totalPtNum + noiseNm));
        System.out.println("总聚合数:" + clusters.size());
        String rstStr = JSON.toJSONString(rstPList);
        String aroundStr = JSON.toJSONString(dbscan.getAroundPoint());

        String resultPath = System.getProperty("user.dir") + File.separator + "result" + File.separator + cityNo + "-result.json";
        String noisePath = System.getProperty("user.dir") + File.separator + "result" + File.separator + cityNo + "-noisePoint.json";
        String aroundPointPath = System.getProperty("user.dir") + File.separator + "result" + File.separator + cityNo + "-aroundPoint.json";
        notesb.append("——————————————————\n");
        notesb.append("城市编号 ： " + cityNo+"\n");
        notesb.append("聚类中的点数为 ： " + dbscan.totalPtNum + " , noiseNum = " + noiseNm + "        ,合计" + (dbscan.totalPtNum + noiseNm)+"\n");
        notesb.append("总聚合数:" + clusters.size()+"\n");
        TxtUtils.writeFile(rstStr, resultPath);
        TxtUtils.writeFile(noiseStr, noisePath);
        TxtUtils.writeFile(aroundStr, aroundPointPath);

        instances = null;
        dbscan.e = new Date();
        long between = dbscan.e.getTime() - dbscan.s.getTime();
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long se = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        System.out.println(cityNo+"运行时间是：" + (day * 24 * 60 + hour * 60 + min) + "分" + se + "秒" +"总毫秒数："+between);
        notesb.append(cityNo+"运行时间是：" + (day * 24 * 60 + hour * 60 + min) + "分" + se + "秒" +"总毫秒数："+between+"\n");
        notesb.append("——————————————————");
        return  notesb.toString();
    }
}


/*
*
* 选中的聚类点数是：357
choose point var lon=120.33545100001842; var lat= 30.296860000069547;
end read -----------------------------------------begin cluster size = 999999
聚类中的点数为 ： 962999 , noiseNum = 37000        ,合计999999
总聚合数:3168
运行时间是：9分35秒



中心点是 ：120.48720200009605       30.17990300008127
var mails =[{"lnglat":[120.3354510000776,30.296860000021493]},{"lnglat":[120.33249200003257,30.29602800007561]},{"lnglat":[120.33249200001595,30.296028000076767]},{"lnglat":[120.3323520000778,30.29656800002021]},{"lnglat":[120.33259000008468,30.29665000006087]},{"lnglat":[120.33266000002457,30.296766000021105]},{"lnglat":[120.33278100000373,30.29693600009825]},{"lnglat":[120.3326720000355,30.296331000075533]},{"lnglat":[120.3324710000527,30.296474000062734]},{"lnglat":[120.33281700000806,30.297049000077397]},{"lnglat":[120.33247200008563,30.29728800005073]},{"lnglat":[120.33247200004364,30.297288000069084]},{"lnglat":[120.33280900005403,30.297558000072556]},{"lnglat":[120.33247200005503,30.297288000051743]},{"lnglat":[120.33247200002413,30.297288000061556]},{"lnglat":[120.33281700000653,30.29704900005307]},{"lnglat":[120.33281700009866,30.297049000069045]},{"lnglat":[120.33281700007682,30.297049000045693]},{"lnglat":[120.33281700009947,30.297049000073848]},{"lnglat":[120.33281700005016,30.297049000097676]},{"lnglat":[120.33281700008493,30.297049000087366]},{"lnglat":[120.33281700002713,30.29704900007189]},{"lnglat":[120.33281700005482,30.297049000022465]},{"lnglat":[120.33281700002236,30.29704900007798]},{"lnglat":[120.33284400007662,30.29795400003903]},{"lnglat":[120.33286300001608,30.298147000089234]},{"lnglat":[120.33286300003452,30.298147000096936]},{"lnglat":[120.33284400008885,30.29795400006211]},{"lnglat":[120.33300000003976,30.296822000036737]},{"lnglat":[120.33300000003156,30.296822000034858]},{"lnglat":[120.33300000006432,30.2968220000835]},{"lnglat":[120.33300000009558,30.296822000051712]},{"lnglat":[120.33300000003067,30.29682200004031]},{"lnglat":[120.33309200000804,30.296932000047736]},{"lnglat":[120.3329320000622,30.29750500001066]},{"lnglat":[120.33284400005287,30.297954000030504]},{"lnglat":[120.33331900008159,30.29699000003481]},{"lnglat":[120.33331900003185,30.29699000003348]},{"lnglat":[120.33337300008601,30.296979000018542]},{"lnglat":[120.33337300002383,30.296979000006765]},{"lnglat":[120.33337300000561,30.296979000027687]},{"lnglat":[120.33331900008444,30.296990000057622]},{"lnglat":[120.33331900009733,30.2969900000648]},{"lnglat":[120.33331900008851,30.296990000039283]},{"lnglat":[120.33511600000944,30.29676000000616]},{"lnglat":[120.33511600000607,30.29676000002456]},{"lnglat":[120.33511600000327,30.296760000030602]},{"lnglat":[120.33511600001624,30.296760000046216]},{"lnglat":[120.33511600001704,30.296760000038326]},{"lnglat":[120.33511600001073,30.296760000068737]},{"lnglat":[120.3351160000117,30.296760000060207]},{"lnglat":[120.33511600000668,30.296760000053023]},{"lnglat":[120.33511600000499,30.296760000073867]},{"lnglat":[120.33511600001174,30.29676000007138]},{"lnglat":[120.33511600000092,30.296760000062356]},{"lnglat":[120.33511600001697,30.296760000016885]},{"lnglat":[120.33511600001881,30.296760000014572]},{"lnglat":[120.33511600003153,30.296760000008717]},{"lnglat":[120.33511600002758,30.296760000037313]},{"lnglat":[120.33511600002772,30.29676000002668]},{"lnglat":[120.3351160000244,30.29676000001537]},{"lnglat":[120.33511600002305,30.296760000028957]},{"lnglat":[120.33511600002933,30.296760000051197]},{"lnglat":[120.33511600002875,30.296760000051073]},{"lnglat":[120.33511600003528,30.296760000044486]},{"lnglat":[120.33511600001763,30.296760000036898]},{"lnglat":[120.33511600004563,30.29676000002462]},{"lnglat":[120.33511600004339,30.296760000037466]},{"lnglat":[120.33511600004206,30.296760000059464]},{"lnglat":[120.33511600005382,30.296760000071032]},{"lnglat":[120.33511600005484,30.29676000005207]},{"lnglat":[120.3351160000469,30.29676000006568]},{"lnglat":[120.33511600005099,30.296760000037533]},{"lnglat":[120.33511600004134,30.296760000069483]},{"lnglat":[120.33511600003938,30.29676000003581]},{"lnglat":[120.3351160000378,30.296760000030993]},{"lnglat":[120.33511600001306,30.29676000009693]},{"lnglat":[120.33511600003686,30.29676000008759]},{"lnglat":[120.33511600000459,30.29676000007933]},{"lnglat":[120.33511600006831,30.296760000001946]},{"lnglat":[120.33511600006571,30.29676000001394]},{"lnglat":[120.33511600006926,30.296760000039576]},{"lnglat":[120.33511600006888,30.29676000001162]},{"lnglat":[120.33511600005772,30.29676000004517]},{"lnglat":[120.33511600006614,30.296760000049506]},{"lnglat":[120.33511600007397,30.29676000005262]},{"lnglat":[120.33511600007279,30.296760000062534]},{"lnglat":[120.33511600006737,30.29676000006371]},{"lnglat":[120.33511600006167,30.29676000005319]},{"lnglat":[120.3351160000656,30.296760000046635]},{"lnglat":[120.33511600008138,30.29676000000643]},{"lnglat":[120.3351160000749,30.296760000007147]},{"lnglat":[120.33511600009996,30.29676000001058]},{"lnglat":[120.33511600008987,30.2967600000079]},{"lnglat":[120.33511600007651,30.2967600000275]},{"lnglat":[120.33511600008724,30.296760000042735]},{"lnglat":[120.33511600008396,30.29676000003272]},{"lnglat":[120.33511600008099,30.2967600000636]},{"lnglat":[120.33511600009433,30.296760000043435]},{"lnglat":[120.33511600009996,30.296760000042514]},{"lnglat":[120.33511600009614,30.29676000001631]},{"lnglat":[120.33511600007472,30.29676000004547]},{"lnglat":[120.33511600007142,30.296760000086984]},{"lnglat":[120.33511600007029,30.29676000007485]},{"lnglat":[120.3351160000666,30.29676000008751]},{"lnglat":[120.3351160000843,30.296760000088437]},{"lnglat":[120.33511600008298,30.296760000099397]},{"lnglat":[120.33518200000117,30.29712800000101]},{"lnglat":[120.33511600008026,30.296760000088472]},{"lnglat":[120.33511600007273,30.296760000078187]},{"lnglat":[120.3351820000143,30.297128000006825]},{"lnglat":[120.33518200002929,30.29712800001197]},{"lnglat":[120.33518200003581,30.29712800003799]},{"lnglat":[120.33518200002902,30.297128000038818]},{"lnglat":[120.33518200001916,30.297128000007902]},{"lnglat":[120.33518200000097,30.29712800007556]},{"lnglat":[120.33518200002716,30.297128000067573]},{"lnglat":[120.33518200007919,30.297128000018276]},{"lnglat":[120.33518200004477,30.29712800005617]},{"lnglat":[120.33518200009148,30.297128000044193]},{"lnglat":[120.33518200009341,30.29712800005728]},{"lnglat":[120.3351820000842,30.297128000052307]},{"lnglat":[120.33518200009998,30.297128000033986]},{"lnglat":[120.33518200004052,30.297129000049246]},{"lnglat":[120.33518200009934,30.2971280000013]},{"lnglat":[120.33511600006487,30.29676000008608]},{"lnglat":[120.33511600009318,30.29676000006676]},{"lnglat":[120.33511600005504,30.29676000008764]},{"lnglat":[120.33545100000411,30.29686000003896]},{"lnglat":[120.33545100002196,30.296860000008124]},{"lnglat":[120.33545100000967,30.296860000006124]},{"lnglat":[120.33545100003616,30.29686000002043]},{"lnglat":[120.33545100002567,30.29686000002968]},{"lnglat":[120.33545100000488,30.296860000005648]},{"lnglat":[120.33545100004426,30.296860000009353]},{"lnglat":[120.33545100004459,30.296860000009417]},{"lnglat":[120.33545100006832,30.296860000043598]},{"lnglat":[120.33545100005077,30.296860000008934]},{"lnglat":[120.33545100004017,30.296860000016604]},{"lnglat":[120.3354510000175,30.296860000087776]},{"lnglat":[120.33545100001774,30.296860000096284]},{"lnglat":[120.33545100000552,30.296860000085424]},{"lnglat":[120.33545100003687,30.29686000005872]},{"lnglat":[120.33545100007001,30.29686000005044]},{"lnglat":[120.33545100006675,30.29686000005058]},{"lnglat":[120.33545100005614,30.296860000071497]},{"lnglat":[120.33545100003786,30.2968600000915]},{"lnglat":[120.33545100004339,30.296860000074606]},{"lnglat":[120.33545100002063,30.29686000007535]},{"lnglat":[120.33545100003285,30.296860000049517]},{"lnglat":[120.33545100008502,30.296860000003285]},{"lnglat":[120.33545100009893,30.296860000041473]},{"lnglat":[120.3354510000792,30.296860000047896]},{"lnglat":[120.33545100009353,30.296860000007946]},{"lnglat":[120.33545100009087,30.29686000009993]},{"lnglat":[120.33545100009748,30.29686000008352]},{"lnglat":[120.33545100008445,30.296860000075142]},{"lnglat":[120.33553000001287,30.29639400005533]},{"lnglat":[120.33553000002878,30.29639400003742]},{"lnglat":[120.33553000006194,30.296394000054327]},{"lnglat":[120.33553000006425,30.29639400000085]},{"lnglat":[120.33553000003639,30.296394000038028]},{"lnglat":[120.33553000005077,30.296394000057152]},{"lnglat":[120.33553000001653,30.296394000079022]},{"lnglat":[120.33553000004457,30.29639400009305]},{"lnglat":[120.33567200005707,30.29641400000818]},{"lnglat":[120.33567200005622,30.296414000043132]},{"lnglat":[120.33567200000715,30.29641400002454]},{"lnglat":[120.3356720000025,30.296414000038595]},{"lnglat":[120.33569400000228,30.29570300004435]},{"lnglat":[120.33569400000592,30.295703000007432]},{"lnglat":[120.33569400000637,30.295703000031093]},{"lnglat":[120.33569400000678,30.295703000029672]},{"lnglat":[120.3356940000053,30.295703000041925]},{"lnglat":[120.33569400002001,30.29570300000949]},{"lnglat":[120.33569400002021,30.29570300003898]},{"lnglat":[120.33569400000994,30.295703000043403]},{"lnglat":[120.3356940000094,30.295703000066474]},{"lnglat":[120.33569400000174,30.29570300008022]},{"lnglat":[120.33569400000138,30.295703000069672]},{"lnglat":[120.33569400002477,30.295703000066798]},{"lnglat":[120.33569400004015,30.295703000008615]},{"lnglat":[120.3356940000327,30.29570300002892]},{"lnglat":[120.33569400003748,30.295703000042984]},{"lnglat":[120.33569400003442,30.29570300004144]},{"lnglat":[120.33569400006833,30.29570300000468]},{"lnglat":[120.33569400006323,30.295703000006398]},{"lnglat":[120.33569400008975,30.295703000024034]},{"lnglat":[120.3356940000782,30.295703000025078]},{"lnglat":[120.33569400005098,30.295703000017784]},{"lnglat":[120.3356940000992,30.29570300002944]},{"lnglat":[120.33569400009871,30.295703000004103]},{"lnglat":[120.33569400008798,30.2957030000349]},{"lnglat":[120.3356940000977,30.295703000034756]},{"lnglat":[120.33569400009794,30.295703000034205]},{"lnglat":[120.33569400004473,30.295703000029977]},{"lnglat":[120.33569400003569,30.295703000061465]},{"lnglat":[120.33569400006218,30.295703000047318]},{"lnglat":[120.33569400006503,30.295703000054992]},{"lnglat":[120.3356940000469,30.29570300007251]},{"lnglat":[120.33569400006,30.295703000090242]},{"lnglat":[120.33569400005736,30.295703000087553]},{"lnglat":[120.33569400003411,30.295703000084494]},{"lnglat":[120.33569400009584,30.295703000072937]},{"lnglat":[120.33569400009037,30.295703000065632]},{"lnglat":[120.33569400008261,30.295703000064435]},{"lnglat":[120.33569400009726,30.295703000091653]},{"lnglat":[120.33569400009422,30.295703000086842]},{"lnglat":[120.33569400007988,30.29570300006619]},{"lnglat":[120.33569400008403,30.29570300004586]},{"lnglat":[120.3356940000291,30.2957030000827]},{"lnglat":[120.33598100006219,30.296063000010598]},{"lnglat":[120.33598100009291,30.29606300002574]},{"lnglat":[120.33598100008835,30.296063000031555]},{"lnglat":[120.33598100006364,30.296063000027758]},{"lnglat":[120.33598100000552,30.296063000041823]},{"lnglat":[120.33598100005838,30.296063000027555]},{"lnglat":[120.33598100002978,30.296063000086924]},{"lnglat":[120.33598100003272,30.296063000069157]},{"lnglat":[120.33598100008359,30.296063000048655]},{"lnglat":[120.33598100008702,30.296063000090644]},{"lnglat":[120.33612800005682,30.298437000058065]},{"lnglat":[120.3361280000904,30.298437000089955]},{"lnglat":[120.3364290000525,30.298250000070873]},{"lnglat":[120.33642900005654,30.298250000075587]},{"lnglat":[120.33676800000724,30.298247000032124]},{"lnglat":[120.33643400007156,30.295340000089205]},{"lnglat":[120.3364290000747,30.29825000008219]},{"lnglat":[120.33675000008195,30.29850000002316]},{"lnglat":[120.33642900005108,30.298250000096516]},{"lnglat":[120.33676800008276,30.29824700006886]},{"lnglat":[120.33676800004716,30.298247000056843]},{"lnglat":[120.33676800004461,30.298247000096236]},{"lnglat":[120.33695100005568,30.29831400006793]},{"lnglat":[120.33676800007304,30.298247000027665]},{"lnglat":[120.33712200009845,30.298341000010353]},{"lnglat":[120.33712200005706,30.29834100000617]},{"lnglat":[120.33712200002886,30.298341000027147]},{"lnglat":[120.33712200003995,30.29834100005086]},{"lnglat":[120.33712200007209,30.29834100001488]},{"lnglat":[120.33712200003075,30.29834100004874]},{"lnglat":[120.33712200002542,30.29834100001235]},{"lnglat":[120.33712200001419,30.298341000035805]},{"lnglat":[120.33690000000078,30.29855800001091]},{"lnglat":[120.33690000000618,30.29855800004544]},{"lnglat":[120.33690000003567,30.298558000037396]},{"lnglat":[120.33690000000513,30.298558000022485]},{"lnglat":[120.33690000008403,30.298558000038216]},{"lnglat":[120.33690000006919,30.298558000038216]},{"lnglat":[120.3369000000877,30.29855800001606]},{"lnglat":[120.3369000000706,30.29855800004822]},{"lnglat":[120.336900000087,30.29855800004184]},{"lnglat":[120.33690000005348,30.298558000039904]},{"lnglat":[120.33690000004924,30.2985580000427]},{"lnglat":[120.33690000001226,30.29855800009272]},{"lnglat":[120.33690000000037,30.29855800008837]},{"lnglat":[120.33690000007832,30.298558000096367]},{"lnglat":[120.33690000003347,30.29855800008922]},{"lnglat":[120.33690000008666,30.298558000054825]},{"lnglat":[120.33712200007308,30.2983410000562]},{"lnglat":[120.33699300004474,30.298576000001088]},{"lnglat":[120.33706500004867,30.298380000060448]},{"lnglat":[120.33712200008443,30.298341000055004]},{"lnglat":[120.3369930000664,30.29857600000494]},{"lnglat":[120.3369930000109,30.298576000005294]},{"lnglat":[120.33699300004234,30.298576000004612]},{"lnglat":[120.3371220000958,30.298341000094602]},{"lnglat":[120.33699300000424,30.29857600004238]},{"lnglat":[120.33699300001122,30.298576000047216]},{"lnglat":[120.33699300001052,30.298576000036512]},{"lnglat":[120.33699300000247,30.29857600001413]},{"lnglat":[120.3369930000123,30.29857600001156]},{"lnglat":[120.33699300002604,30.29857600006845]},{"lnglat":[120.33699300001938,30.298576000037]},{"lnglat":[120.33699300003306,30.298576000071748]},{"lnglat":[120.3369930000317,30.29857600003254]},{"lnglat":[120.3369930000166,30.29857600002186]},{"lnglat":[120.33699300001184,30.2985760000462]},{"lnglat":[120.3369930000131,30.298576000075013]},{"lnglat":[120.33699300000679,30.298576000079237]},{"lnglat":[120.33699300000676,30.298576000072472]},{"lnglat":[120.33699300005345,30.298576000023523]},{"lnglat":[120.33699300004628,30.29857600005806]},{"lnglat":[120.33699300004491,30.29857600006688]},{"lnglat":[120.3369930000171,30.29857600008453]},{"lnglat":[120.33699300003894,30.29857600009188]},{"lnglat":[120.33699300003425,30.298576000089103]},{"lnglat":[120.33699300004261,30.29857600009809]},{"lnglat":[120.33699300005132,30.298576000088]},{"lnglat":[120.33699300002128,30.298576000094677]},{"lnglat":[120.33699300000849,30.298576000079294]},{"lnglat":[120.336993000066,30.29857600001465]},{"lnglat":[120.33699300006369,30.298576000012826]},{"lnglat":[120.33699300006839,30.298576000028596]},{"lnglat":[120.33699300007451,30.298576000026653]},{"lnglat":[120.33699300007277,30.298576000036327]},{"lnglat":[120.3369930000711,30.29857600003265]},{"lnglat":[120.33699300006819,30.29857600002989]},{"lnglat":[120.33699300006619,30.298576000046044]},{"lnglat":[120.33699300006249,30.298576000014798]},{"lnglat":[120.33699300009877,30.298576000019327]},{"lnglat":[120.3369930000868,30.29857600004008]},{"lnglat":[120.33699300009502,30.298576000036157]},{"lnglat":[120.33699300009458,30.29857600003648]},{"lnglat":[120.33699300009532,30.29857600004258]},{"lnglat":[120.33699300008645,30.29857600004181]},{"lnglat":[120.33699300007795,30.298576000030774]},{"lnglat":[120.33699300007568,30.29857600002527]},{"lnglat":[120.33699300006302,30.298576000052236]},{"lnglat":[120.33699300007864,30.298576000055068]},{"lnglat":[120.33699300008936,30.298576000049895]},{"lnglat":[120.33699300008296,30.29857600005765]},{"lnglat":[120.33699300009253,30.29857600008157]},{"lnglat":[120.3369930000619,30.298576000070366]},{"lnglat":[120.33699300009371,30.29857600009225]},{"lnglat":[120.33699300009498,30.298576000056674]},{"lnglat":[120.33719300007354,30.298841000000106]},{"lnglat":[120.33699300009926,30.298576000057977]},{"lnglat":[120.33719300008951,30.29884100004849]},{"lnglat":[120.33719300008734,30.298841000025266]},{"lnglat":[120.33719300004839,30.298841000050672]},{"lnglat":[120.33719300006655,30.298841000019213]},{"lnglat":[120.33699300009502,30.298576000092144]},{"lnglat":[120.33719300001643,30.298841000069714]},{"lnglat":[120.33722500002496,30.298918000005862]},{"lnglat":[120.33722500001177,30.2989180000124]},{"lnglat":[120.33719300002772,30.298841000068276]},{"lnglat":[120.33722500003776,30.298918000002555]},{"lnglat":[120.33699300006519,30.298576000048378]},{"lnglat":[120.33699300005378,30.29857600006696]},{"lnglat":[120.33699300009445,30.298576000005447]},{"lnglat":[120.33690700009359,30.29840800009654]},{"lnglat":[120.33712200008283,30.298341000052734]},{"lnglat":[120.33676800003673,30.29824700009082]},{"lnglat":[120.3372250000102,30.29891800003795]},{"lnglat":[120.33722500000509,30.298918000041834]},{"lnglat":[120.33722500004131,30.298918000053416]},{"lnglat":[120.33722500004363,30.29891800006427]},{"lnglat":[120.33722500003377,30.29891800005796]},{"lnglat":[120.33722500003337,30.29891800007319]},{"lnglat":[120.33722500001976,30.298918000067275]},{"lnglat":[120.3372250000487,30.298918000073602]},{"lnglat":[120.33722500002348,30.298918000018293]},{"lnglat":[120.33722500006509,30.29891800002377]},{"lnglat":[120.33722500005719,30.298918000069083]},{"lnglat":[120.337225000054,30.298918000040597]},{"lnglat":[120.33722500007303,30.29891800001066]},{"lnglat":[120.33728400008737,30.29561700003813]},{"lnglat":[120.33741000000514,30.298498000034623]},{"lnglat":[120.3374100000118,30.298498000006518]},{"lnglat":[120.33741000001639,30.29849800007604]},{"lnglat":[120.33741000001693,30.29849800004751]},{"lnglat":[120.33741000002192,30.29849800002626]},{"lnglat":[120.33741000001012,30.29849800006698]},{"lnglat":[120.33741000002932,30.298498000006493]},{"lnglat":[120.3374100000611,30.298498000000354]},{"lnglat":[120.33741000003269,30.298498000021773]},{"lnglat":[120.33741000008303,30.29849800000654]},{"lnglat":[120.33741000003023,30.29849800000701]},{"lnglat":[120.3374100000274,30.29849800003099]},{"lnglat":[120.33741000003775,30.298498000052266]},{"lnglat":[120.33741000004102,30.29849800005397]},{"lnglat":[120.33741000003769,30.29849800007502]},{"lnglat":[120.33741000004355,30.2984980000393]},{"lnglat":[120.33741000005153,30.29849800004811]},{"lnglat":[120.33741000005199,30.29849800004009]},{"lnglat":[120.33741000006141,30.298498000034208]},{"lnglat":[120.33741000006025,30.29849800006842]},{"lnglat":[120.33741000007667,30.298498000024896]},{"lnglat":[120.33741000008686,30.29849800006896]},{"lnglat":[120.33741000008732,30.298498000047488]},{"lnglat":[120.33741000008396,30.298498000033252]},{"lnglat":[120.33741000005725,30.298498000086813]},{"lnglat":[120.33741000005612,30.298498000091882]},{"lnglat":[120.33741000004461,30.298498000076485]},{"lnglat":[120.33741000004399,30.298498000072577]},{"lnglat":[120.33741000002851,30.29849800002295]},{"lnglat":[120.3374100000253,30.29849800006472]},{"lnglat":[120.33722500009392,30.29891800003496]},{"lnglat":[120.3372250000816,30.298918000061533]},{"lnglat":[120.33722500009077,30.298918000086655]},{"lnglat":[120.337225000079,30.298918000076235]},{"lnglat":[120.33722500008774,30.29891800006566]},{"lnglat":[120.33722500009725,30.298918000078483]},{"lnglat":[120.33722500009408,30.2989180000833]},{"lnglat":[120.33722500007094,30.298918000010815]},{"lnglat":[120.33741000009785,30.298498000021798]},{"lnglat":[120.33741000009388,30.298498000024235]},{"lnglat":[120.33741000009178,30.298498000042315]},{"lnglat":[120.33741000008928,30.298498000098494]},{"lnglat":[120.33741000009428,30.298498000089857]},{"lnglat":[120.33741000008861,30.298498000056952]},{"lnglat":[120.33741000009337,30.29849800005105]},{"lnglat":[120.33750000004015,30.29545000001288]},{"lnglat":[120.33741000008779,30.298498000059638]},{"lnglat":[120.3372250000681,30.298918000065377]},{"lnglat":[120.33764800004097,30.295411000005362]},{"lnglat":[120.33764800001973,30.295411000014003]},{"lnglat":[120.33764800004366,30.295411000074896]},{"lnglat":[120.33764800007123,30.29541100008361]},{"lnglat":[120.33764800000928,30.295411000081007]},{"lnglat":[120.33764800003746,30.295411000036644]},{"lnglat":[120.33766000006551,30.298620000047695]},{"lnglat":[120.3376480000879,30.295411000090088]},{"lnglat":[120.33766000003949,30.29862000007394]},{"lnglat":[120.33764800008748,30.295411000011242]},{"lnglat":[120.33764800005365,30.295411000000424]},{"lnglat":[120.33766000007154,30.29862000004621]},{"lnglat":[120.33766000008913,30.298620000003233]},{"lnglat":[120.33766000006935,30.298620000090903]}]
选中的聚类点数是：410
choose point var lon=120.3354510000776; var lat= 30.296860000021493;
end read -----------------------------------------begin cluster size = 3899999
聚类中的点数为 ： 3504365 , noiseNum = 395617        ,合计3899982
总聚合数:4040
运行时间是：121分43秒
* */
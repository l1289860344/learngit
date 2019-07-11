package zy.core.clustering.distance;

import com.alibaba.fastjson.JSONArray;
import edu.princeton.cs.algs4.RectHV;
import lombok.Data;
import zy.core.Point;
import zy.core.clustering.Dbscan;
import zy.core.clustering.SysProperty;
import zy.core.json.ReadUtils;
import zy.core.txt_utils.PropertiesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CheckDistance4Noise {

    private double DISTANT ;

    private Point centerP = new Point(120.20211500003046, 30.255360000034642);
    private List<Point> pList = new ArrayList<Point>();

    private String filePath = "checkPointDis.json";

    private String noiseFile = "noisePoint.json";
    private List<Point> noiseList = null;
    private String aroundFile = "aroundPoint.json";
    private List<Point> aroundList = null;

    public void readNoise() {
        System.out.println("读取Noise中");
        //String path = ReadUtils.class.getClassLoader().getResource(this.noiseFile).getPath();
        String path= System.getProperty("user.dir") + File.separator + "result" + File.separator + "330100-"+this.noiseFile;
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List points = jsonarray.toJavaList(Point.class);
//        JSONObject jobj = JSON.parseObject(s);
        noiseList = points;
        System.out.println("读取Noise完毕");
       System.out.println("点数为：" + points.size());
    }


    public void readAround() {
        System.out.println("读取Around中");
        String path= System.getProperty("user.dir") + File.separator + "result" + File.separator + "330100-"+this.aroundFile;
//        ReadUtils.class.getClassLoader().
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List points = jsonarray.toJavaList(Point.class);
//        JSONObject jobj = JSON.parseObject(s);
        aroundList = points;
        System.out.println("读取Around完毕");
       System.out.println("点数为：" + points.size());
    }


    public void readPoint() {
        String path = ReadUtils.class.getClassLoader().getResource(this.filePath).getPath();
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List points = jsonarray.toJavaList(Point.class);
//        JSONObject jobj = JSON.parseObject(s);
        pList = points;
//        System.out.println(points.toString());
    }


    public static void main(String[] args) {
        SysProperty sys = PropertiesUtil.readAppProperty();

        CheckDistance4Noise checkDistance = new CheckDistance4Noise();
        checkDistance.DISTANT = sys.getRadius();
        checkDistance.readNoise();
        checkDistance.readAround();

        int count = 0;
        int Aroundcount = 0;
        System.out.println("一共验证"+checkDistance.getAroundList().size()+" 个噪音");
        System.out.println("一共验证"+checkDistance.getNoiseList().size()+" 个噪音");
        Date sdate=new Date();

        for (Point around : checkDistance.getAroundList()) {
            System.out.println("正在验证第"+ Aroundcount++ +" 个中心点");
            LngLat s = new LngLat(around.getX(), around.getY());
            double[] range = LatLonUtil.getAround(s, sys.getRadius());
            RectHV rect = new RectHV(range[1], range[0], range[3], range[2]);
            for (Point p : checkDistance.getNoiseList()) {
                LngLat e = new LngLat(p.getX(), p.getY());
                double dis = LatLonUtil.getDistance(s, e,rect,301);
//            System.out.println(dis + "， 标准是：" + Dbscan.DISTANT);
                if (dis <= sys.getRadius()) {
                    count = count + 1;
                    System.out.println(dis + " < " + sys.getRadius());
                    System.out.println("not valid noise is = " + p.getX() + "--" + p.getY() + ", around point :" + around.getX() + "--" + around.getY());
                }
            }

        }
        Date edate=new Date();
        System.out.println("一共验证花费 "+(sdate.getTime()-edate.getTime())+" 毫秒");
        System.out.println("一共验证"+checkDistance.getAroundList().size()+" 个噪音");
        System.out.println("一共验证"+checkDistance.getNoiseList().size()+" 个噪音");
        System.out.println("验证点数是:" + checkDistance.getNoiseList().size());
        System.out.println("不合格点数是：" + count);

      /*  Double lat1 = 34.264648;  //纬度

        Double lon1 = 108.952736;  //经度

        LngLat start = new LngLat(116.368904, 39.923423);
        LngLat end = new LngLat(116.387271, 39.922501);

        int radius = 1000;  //半径

        double around[] = getAround(start, radius);

        for (int i = 0; i < around.length; i++) {
            System.out.println(around[i]);
        }

        System.out.println("-----------");
//计算2个坐标点的距离
        System.out.println(LatLonUtil.getDistance(start, end));

//验证计算出来的坐标点，是不是指定的距离
        LatLonUtil.checkDis(around, start);

        LngLat s = new LngLat(119.81204500002877, 30.44763600001973);
        LngLat e = new LngLat(119.80679100002256, 30.451613000082336);

        System.out.println(LatLonUtil.getDistance(s, e));*/
    }
}

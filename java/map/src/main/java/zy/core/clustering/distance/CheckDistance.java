package zy.core.clustering.distance;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import zy.core.Point;
import zy.core.clustering.Dbscan;
import zy.core.clustering.DbscanDirectly;
import zy.core.clustering.SysProperty;
import zy.core.json.ReadUtils;
import zy.core.txt_utils.PropertiesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class CheckDistance {
    //e{"lnglat":[120.16362800001748,30.248916000093253]}
    //最大距离为，指定距离的对角线长度
//    private double DISTANT = Math.sqrt(Dbscan.DISTANT * Dbscan.DISTANT + Dbscan.DISTANT * Dbscan.DISTANT);
    public double DISTANT;

    //    private Point centerP = new Point(120.16052004298265, 30.27988570998357);
    //下载的中心点不是重心点，是聚类第一个点 120.16710600002797--30.248410000057437
    private Point centerP = new Point(120.209190,30.206150);
    private List<Point> pList = new ArrayList<Point>();

    private String filePath = "checkPointDis.json";

    public void readPoint() {
        String path = System.getProperty("user.dir") + File.separator + "result" + File.separator + "330100" + "-ClusterPath.json";

        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List points = jsonarray.toJavaList(Point.class);
//        JSONObject jobj = JSON.parseObject(s);
        pList = points;
//        System.out.println(points);
    }


    public static void main(String[] args) {
        SysProperty sys = PropertiesUtil.readAppProperty();
        CheckDistance checkDistance = new CheckDistance();

        checkDistance.readPoint();
        System.out.println(checkDistance.pList.get(1));

        int count = 0;
        checkDistance.DISTANT= sys.getRadius();
        for (Point p : checkDistance.pList) {
            LngLat s = new LngLat(checkDistance.centerP.getX(), checkDistance.getCenterP().getY());
            LngLat e = new LngLat(p.getX(), p.getY());
            double dis = LatLonUtil.getDistance(s, e);
            System.out.println(dis + "， 标准是：" + sys.getRadius());

            if (dis > checkDistance.DISTANT) {
                count = count + 1;
                System.out.println(dis + " > " + sys.getRadius());
                System.out.println("not valid point is = " + p.getX() + "           " + p.getY());
            }
        }
        System.out.println("验证点数是:" + checkDistance.getPList().size());
        System.out.println("不合格点数是：" + count);

//        {"lnglat":[120.16362800001748,30.248916000093253]}


/*        LngLat e = new LngLat(120.21968500001003, 30.256624000085804);
        LngLat s = new LngLat(120.22193000008338, 30.252747000049087);
        double dis = LatLonUtil.getDistance(s, e);
        System.out.println(dis + "， 标准是：" + Dbscan.DISTANT);*/


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
*/
      //120.18235800016375
        //30.24889400009871
    /*    System.out.println("------------------------");
        LngLat s = new LngLat(119.988688, 30.375048);
        LngLat e = new LngLat(119.988355, 30.375423);

        System.out.println(LatLonUtil.getDistance(s, e));*/
    }
}

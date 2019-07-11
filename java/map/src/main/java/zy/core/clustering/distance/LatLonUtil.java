package zy.core.clustering.distance;


import edu.princeton.cs.algs4.RectHV;

public class LatLonUtil {
    private static final double PI = Math.PI;

    private static final double EARTH_RADIUS = 6378.137;

    private static final double RAD = Math.PI / 180.0;

    /**
     * @param raidus 单位米
     *               <p>
     *               指定位置（某一处的经纬度lng lat）和搜索半径（r），搜索指定范围内的数据
     *               <p>
     *               如果对精度要求不是很高，可以根据指定位置的经纬度和半径计算出经纬度的范围，然后判断DB中的经纬度是否在此范围内
     *               <p>
     *               根据map提供的计算两个坐标之间距离的方法，逐一计算指定位置和我们DB库中的位置的距离s，用s和r进行比较，如果s<r，则在搜索范围内，
     *               返给前段标注在地图上。这种方法如果数据量小，可以尝试，如果数据量大，没测试过，但可以想象
     *               <p>
     *               return minLat,minLng,maxLat,maxLng
     *               <p>
     *               param lat 纬度 lon 经度 raidus 单位米
     */
    public static double[] getAround(LngLat point, double raidus) {
        Double latitude = point.latitude;

        Double longitude = point.longitude;

        Double degree = (24901 * 1609) / 360.0;

        double raidusMile = raidus;

        Double dpmLat = 1 / degree;

        Double radiusLat = dpmLat * raidusMile;

        Double minLat = latitude - radiusLat;

        Double maxLat = latitude + radiusLat;

        Double mpdLng = degree * Math.cos(latitude * (PI / 180));

        Double dpmLng = 1 / mpdLng;

        Double radiusLng = dpmLng * raidusMile;

        Double minLng = longitude - radiusLng;

        Double maxLng = longitude + radiusLng;

        return new double[]{minLat, minLng, maxLat, maxLng};
    }

    //验证坐标和距离的关系
    public static void checkDis(double[] area, LngLat center) {
        double l = area[3];
        double lat = (area[2] - area[0]) / 2.0 + area[0];
        LngLat point = new LngLat(l, lat);
        double dis = LatLonUtil.getDistance(center, point);
        System.out.println("核对长度 ： " + dis);
    }


    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     *
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double getDistance(LngLat start, LngLat end) {
        {
            double long1 = start.longitude;
            double long2 = end.longitude;

            double lat1 = start.latitude;
            double lat2 = end.latitude;

            double a, b, d, sa2, sb2;
            lat1 = rad(lat1);
            lat2 = rad(lat2);
            a = lat1 - lat2;
            b = rad(long1 - long2);

            sa2 = Math.sin(a / 2.0);
            sb2 = Math.sin(b / 2.0);
            d = 2 * EARTH_RADIUS
                    * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
                    * Math.cos(lat2) * sb2 * sb2));
            return d * 1000;
        }
    }
    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     *
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double getDistance(LngLat start, LngLat end,RectHV rect,double radius) {
        {
            double long1 = start.longitude;
            double long2 = end.longitude;

            double lat1 = start.latitude;
            double lat2 = end.latitude;

            if(long2>rect.xmax()||long2<rect.xmin()||lat2>rect.ymax()||lat2<rect.ymin()){
                return radius+1;
            }
            double a, b, d, sa2, sb2;
            lat1 = rad(lat1);
            lat2 = rad(lat2);
            a = lat1 - lat2;
            b = rad(long1 - long2);

            sa2 = Math.sin(a / 2.0);
            sb2 = Math.sin(b / 2.0);
            d = 2 * EARTH_RADIUS
                    * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
                    * Math.cos(lat2) * sb2 * sb2));
            return d * 1000;
        }
    }
    public static double getDistance2(LngLat start, LngLat end) {
        {
            double long1 = start.longitude;
            double long2 = end.longitude;

            double lat1 = start.latitude;
            double lat2 = end.latitude;

            double cos = Math.cos(long1) * Math.cos(long2) * Math.cos(lat1 - lat1)
                    + Math.sin(long1) * Math.sin(long2);
//        System.out.println("cos = " + cos); // 值域[-1,1]
            double acos = Math.acos(cos); // 反余弦值
//        System.out.println("acos = " + acos); // 值域[0,π]
//        System.out.println("∠AOB = " + Math.toDegrees(acos)); // 球心角 值域[0,180]
            return EARTH_RADIUS * acos; // 最终结果
        }
    }

    public static void main(String[] args) {
        Double lat1 = 34.264648;  //纬度

        Double lon1 = 108.952736;  //经度

        LngLat start = new LngLat(116.368904, 39.923423);
        LngLat end = new LngLat(116.387271, 39.922501);

        int radius = 1000;  //半径

        double around[] = getAround(start, radius);

        for (int i = 0; i < around.length; i++) {
            System.out.println(around[i]);
        }
        System.out.println(start.latitude - around[0]);
        System.out.println(start.latitude - around[2]);
        System.out.println(start.longitude - around[1]);
        System.out.println(start.longitude - around[3]);
    //return new double[]{minLat, minLng, maxLat, maxLng};

        System.out.println("-----------");
//计算2个坐标点的距离
//        System.out.println(getDistance(start, end));

//验证计算出来的坐标点，是不是指定的距离
//        checkDis(around, start);

//        LngLat s = new LngLat(120.427651, 30.232133);
//        LngLat e = new LngLat(119.550339, 29.615036);
//        LngLat s = new LngLat(120.427651, 30.232133);
//        LngLat e = new LngLat(121.282704, 31.10022);

//        LngLat s = new LngLat(119.81204500002877, 30.44763600001973);
//        LngLat e = new LngLat(119.80679100002256, 30.451613000082336);
//
//        System.out.println(getDistance(s, e));
    }
}
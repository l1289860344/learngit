/**
 * @ProjectName: map
 * @Package: PACKAGE_NAME
 * @ClassName: GeoHashEx
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/8 10:26
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/8 10:26
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.GeoHash;

public class GeoHashEx {

    public static void main(String[] args) {
        double lat = 30.549608; // 纬度坐标
        double lon = 114.376971; // 经度坐标

        int precision = 8; // Geohash编码字符的长度（最大为12）
        GeoHash geoHash = GeoHash.withCharacterPrecision(lat, lon, precision);

        System.out.println("WGS84Point = ");
        System.out.println( geoHash.getPoint());

        String binaryCode = geoHash.toBinaryString(); // 使用给定的经纬度坐标生成的二进制编码
        System.out.println("经纬度坐标： (" + lat + ", " + lon + ")");
        System.out.println("二进制编码：" + binaryCode);
        String hashCode = geoHash.toBase32(); // 使用给定的经纬度坐标生成的Geohash字符编码
        System.out.println("Geohash编码：" + hashCode);

        // 从二进制的编码中分离出经度和纬度分别对应的二进制编码
        char[] binaryCodes = binaryCode.toCharArray();
        List<Character> latCodes = new ArrayList<Character>();
        List<Character> lonCodes = new ArrayList<Character>();
        for (int i = 0; i < binaryCodes.length; i++) {
            if (i % 2 == 0) {
                lonCodes.add(binaryCodes[i]);
            } else {
                latCodes.add(binaryCodes[i]);
            }
        }
        StringBuilder latCode = new StringBuilder(); // 纬度对应的二进制编码
        StringBuilder lonCode = new StringBuilder(); // 经度对应的二进制编码
        for (Character ch : latCodes) {
            latCode.append(ch);
        }
        for (Character ch : lonCodes) {
            lonCode.append(ch);
        }

        System.out.println("纬度二进制编码：" + latCode.toString());
        System.out.println("经度二进制编码：" + lonCode.toString());

//        GeoHash.
    }
}
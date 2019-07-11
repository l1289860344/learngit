package zy;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * @ProjectName: map
 * @Package: zy
 * @ClassName: StringUtils
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/14 21:50
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/14 21:50
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class StringUtils {
    public static void main(String[] args) {
       /* String px = "120.16710600002797";
        String py = "30.248410000057437";
        System.out.println(py.substring(0,9));
        if ("30.248410".equals(py.substring(0, 9))) {
            System.out.println(true);
        }*/

        System.out.println(StringUtils.toCoordDouble(33.12345678));
    }

    //保留 小数点6位，去一法
    public static double toCoordDouble(double d) {
        NumberFormat nf = NumberFormat.getNumberInstance();
// 保留两位小数
        nf.setMaximumFractionDigits(6);
// 如果不需要四舍五入，可以使用RoundingMode.DOWN
        nf.setRoundingMode(RoundingMode.DOWN);
//        System.out.println(nf.format(d));
        return Double.parseDouble(nf.format(d));
    }
}

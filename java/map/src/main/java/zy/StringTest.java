package zy;

/**
 * @ProjectName: map
 * @Package: zy
 * @ClassName: StringTest
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/14 21:50
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/14 21:50
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class StringTest {
    public static void main(String[] args) {
        String px = "120.16710600002797";
        String py = "30.248410000057437";
        System.out.println(py.substring(0,9));
        if ("30.248410".equals(py.substring(0, 9))) {
            System.out.println(true);
        }
    }
}

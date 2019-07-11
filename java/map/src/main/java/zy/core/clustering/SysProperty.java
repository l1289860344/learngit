package zy.core.clustering;

import lombok.Data;

import java.util.ArrayList;

/**
 * @ProjectName: map
 * @Package: zy.core.clustering
 * @ClassName: SysProperty
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/23 16:30
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/23 16:30
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Data
public class SysProperty {
    private ArrayList<String> cityList;
    private double radius;
    private int minMailCount;
    private int maxThreadCount;
}

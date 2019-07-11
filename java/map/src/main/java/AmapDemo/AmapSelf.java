package AmapDemo;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class AmapSelf {
    private AMap mAMap;
//    private Context mContext;
    private List<ClusterItem> mClusterItems;  //坐标点数组
    private List<Cluster> mClusters;//聚合点数组
    private int mClusterSize;   //聚合范围的大小

    private List<Marker> mAddMarkers = new ArrayList<Marker>();
    private double mClusterDistance;  //聚合距离
    private float mPXInMeters;
    private boolean mIsCanceled = false;




    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     * @param clusterItem
     */
    private void calculateSingleCluster(ClusterItem clusterItem) {
        LatLng latlng = clusterItem.getPosition();

        Cluster cluster = getCluster(latlng,mClusters);
        if (cluster != null) {
            cluster.addClusterItem(clusterItem);
        } else {
            cluster = new Cluster(latlng);
            mClusters.add(cluster);
            cluster.addClusterItem(clusterItem);
        }
    }


    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     *
     * @param latLng
     * @return
     */
    private Cluster getCluster(LatLng latLng,List<Cluster>clusters) {
        for (Cluster cluster : clusters) {
            LatLng clusterCenterPoint = cluster.getCenterLatLng();
            double distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint);
            if (distance < mClusterDistance) {
                return cluster;
            }
        }
        return null;
    }
}

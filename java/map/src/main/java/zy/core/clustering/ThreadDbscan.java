package zy.core.clustering;

import com.clustering.Data;
import zy.core.clustering.Dbscan;
import zy.core.clustering.SysProperty;
import zy.core.txt_utils.PropertiesUtil;
import zy.core.txt_utils.TxtUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadDbscan {

    public static void main(String[] args) {
        SysProperty sys = PropertiesUtil.readAppProperty();
        Date s=new Date();
        String notePointPath = System.getProperty("user.dir") + File.separator + "result" + File.separator + "AllCity-" +(s.getYear()+1900)+"."+(s.getMonth()+1)+"."+s.getDate()+"."+s.getHours()+"."+s.getMinutes()+"."+s.getSeconds()+ "-notePoint.json";
        System.out.println(notePointPath);
        StringBuffer  notesb=new StringBuffer("");
        List<String>  cityList = sys.getCityList();

        cityList.forEach((cityNo) -> {
            notesb.append(  Dbscan.pushdata(cityNo,sys));
        });
        notesb.append("总运行时间"+getPastTime(s)+"\n");
        TxtUtils.writeFile(notesb.toString(), notePointPath);
    }
    public static String getPastTime(Date s) {
         Date e= new Date();
        long between = e.getTime() - s.getTime();
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long se = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return ("" + (day * 24 * 60 + hour * 60 + min) + "分" + se + "秒");
    }
}

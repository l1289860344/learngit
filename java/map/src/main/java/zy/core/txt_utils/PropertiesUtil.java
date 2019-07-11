package zy.core.txt_utils;

import zy.StringUtils;
import zy.core.clustering.SysProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * @ProjectName: map
 * @Package: zy.core.txt_utils
 * @ClassName: PropertiesUtil
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/23 16:21
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/23 16:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PropertiesUtil {

    public static SysProperty readAppProperty() {
        Properties prop = new Properties();
        SysProperty sys = new SysProperty();
        String fileName = System.getProperty("user.dir") + File.separator + "app.properties";
        try {
            //读取属性文件a.properties
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);     ///加载属性列表
//            System.out.println(prop);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            while (it.hasNext()) {
                String key = it.next();
//                System.out.println(key + ":" + prop.getProperty(key));
                if (key.equals("radius")) {
                    sys.setRadius(Double.parseDouble(prop.getProperty(key)));
                } else if (key.equals("minMailCount")) {
                    sys.setMinMailCount(Integer.parseInt(prop.getProperty(key)));
                }else if (key.equals("maxThreadCount")) {
                    sys.setMaxThreadCount(Integer.parseInt(prop.getProperty(key)));
                } else if (key.equals("cityNo")) {
                    ArrayList<String> cityList = new ArrayList<String>();
                    String cities = prop.getProperty(key);
                    String[] strList = cities.split(",");
                    for (String str : strList) {
                        cityList.add(str);
                    }
                    sys.setCityList(cityList);
                }
            }
            in.close();

            return sys;
            ///保存属性到b.properties文件
          /*  FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
            prop.setProperty("phone", "10086");
            prop.store(oFile, "The New properties file");
            oFile.close();*/
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static void main(String[] args) {
        PropertiesUtil p = new PropertiesUtil();
        System.out.println(p.readAppProperty());

        /*Properties prop = new Properties();
        String fileName = System.getProperty("user.dir") + File.separator + "app.properties";
        try {
            //读取属性文件a.properties
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);     ///加载属性列表
            System.out.println(prop);
            Iterator<String> it = prop.stringPropertyNames().iterator();
            while (it.hasNext()) {
                String key = it.next();
                System.out.println(key + ":" + prop.getProperty(key));
            }
            in.close();

            ///保存属性到b.properties文件
            FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
            prop.setProperty("phone", "10086");
            prop.store(oFile, "The New properties file");
            oFile.close();
        } catch (Exception e) {
            System.out.println(e);
        }*/
    }

   /* public static void main(String[] args) {
        Properties pro = new Properties();
        FileInputStream in = null;
        try {
            String fileName = System.getProperty("user.dir") + File.separator  + "app.properties";
            in = new FileInputStream(fileName);
            pro.load(in);
            System.out.println(pro);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
}

package zy.core.json;

import com.alibaba.fastjson.JSONArray;
import zy.core.Point;

import java.io.*;
import java.util.List;

public class ReadUtils {
    /**
     * 读取json文件，返回json串
     *
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
//            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String path = ReadUtils.class.getClassLoader().getResource("10030.json").getPath();
        String s = ReadUtils.readJsonFile(path);
        JSONArray jsonarray = JSONArray.parseArray(s);
        List points = jsonarray.toJavaList(Point.class);
//        JSONObject jobj = JSON.parseObject(s);

        System.out.println(points);

   /*     System.out.println("name" + jobj.get("name"));
        JSONObject address1 = jobj.getJSONObject("address");
        String street = (String) address1.get("street");
        String city = (String) address1.get("city");
        String country = (String) address1.get("country");

        System.out.println("street :" + street);
        System.out.println("city :" + city);
        System.out.println("country :" + country);

        JSONArray links = jobj.getJSONArray("links");

        for (int i = 0; i < links.size(); i++) {
            JSONObject key1 = (JSONObject) links.get(i);
            String name = (String) key1.get("name");
            String url = (String) key1.get("url");
            System.out.println(name);
            System.out.println(url);
        }*/
    }
}

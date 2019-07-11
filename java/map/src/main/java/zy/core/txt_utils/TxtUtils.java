package zy.core.txt_utils;

import java.io.*;

/**
 * @ProjectName: map
 * @Package: zy.core.txt_utils
 * @ClassName: TxtUtils
 * @Description: java类作用描述
 * @Author: peter.M
 * @CreateDate: 2019/6/14 20:11
 * @UpdateUser: peter.M
 * @UpdateDate: 2019/6/14 20:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TxtUtils {

    public static void main(String args[]) {
//        readFile();
        System.out.println(System.getProperty("user.dir"));
        String str = "aaaaaaaaa";
        writeFile(str, "D:\\code\\study\\map\\result\\10030.txt");
    }

    public static void writeFile(String str, String fileName) {
        /* 写入Txt文件 */
//        String fileName = ReadUtils.class.getClassLoader().getResource("noisePoint.txt").getPath();
        File filePath = new File( fileName);
//        File filePath = new File(fileName);
//        File filePath = new File(“F:\\eclipse-workspace\\三角形测试\\\\src\\triangleTest\\DataofTest.txt");
        try {
            System.out.println(fileName);
//            filePath.createNewFile(); // 创建新文件
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath, false));
            out.write(str);
            out.flush(); // 清空缓冲区
            out.close(); // //不关闭文件会导致资源的泄露，读写文件都同理
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
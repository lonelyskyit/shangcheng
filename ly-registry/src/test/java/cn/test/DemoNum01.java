package cn.test;

import java.util.Scanner;

/**
 * @ClassName: DemoNum01
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/23/023  14:40
 * @Version: 1.0
 */
public class DemoNum01 {
    public static void main(String[] args) {
      /*  String s1 = new String("ABC");
        String s2 = new String("ABC");
        System.out.println(s1==(s2));
        long a=444;
      String x='a';
        char y="a";*/
      //打印等腰三角形
        System.out.println("请输入三角形行数：");
        Scanner scanner = new Scanner(System.in);
        int count = scanner.nextInt();
        for (int x = 1; x <= count; x++) {
            for (int y = 1; y <= count - x; y++) {
                System.out.print(" ");
            }
            for (int z = 1; z <= 2 * x - 1; z++) {
                System.out.print("*");
            }
            System.out.println();
        }
//        Collections.synchronizedList([args]);

    }
}

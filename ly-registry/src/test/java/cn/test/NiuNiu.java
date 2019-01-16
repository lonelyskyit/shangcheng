package cn.test;

import java.util.Arrays;
import java.util.Random;

/**
 * @ClassName: NiuNiu
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/22/022  15:02
 * @Version: 1.0
 */
public class NiuNiu {
    public static void main(String[] args) {
        Random random = new Random();
        int[] arr = new int[5];
        for (int i=0;i<5 ;i++) {
            arr[i] = random.nextInt(10) + 1;
        }
        System.out.println("arr = " + Arrays.toString(arr));
        for (int m = 0; m < 5; m++) {
            for (int n = 0; n < 5 && n != m; n++) {
                for (int x = 0; x < 5 && x != m && x != n; x++) {
                    if ((arr[m] + arr[n] + arr[x]) % 10 == 0) {
                        int sum=0;
                        for (int y = 0; y < 5 ; y++) {
                            if (y != m && y != n && y != x) {
                                sum += arr[y];
                            }
                        }
                        if (sum == 10) {
                            System.out.println("牛牛！");
                            break;
                        }
                        System.out.println("这次牌是：牛"+(sum%10));
                    }
                }
            }
        }

    }

}

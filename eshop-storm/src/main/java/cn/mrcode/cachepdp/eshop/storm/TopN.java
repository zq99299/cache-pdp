package cn.mrcode.cachepdp.eshop.storm;

import org.apache.storm.shade.org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2019/5/23 21:33
 */
public class TopN {
    public static void main(String[] args) {
        topn2();
    }

    public static void topn1() {
        /**
         * top n 简易算法：手写思路
         * top 3 列表： 5、3、1
         * 比如来一个 6，那么比 5 大，把 5 开头的往后移位。最后把 6 放到 第一位
         */

        int n = 10;
        int[] topn = new int[n];

        // 循环 n 次，模拟有这么多数据需要计算
        for (int i = 0; i < 100; i++) {
            int randomNum = RandomUtils.nextInt(100);
//            int randomNum = i;
            // 每次都从第一个开始比较
            for (int j = 0; j < topn.length; j++) {
                int target = topn[j];
                if (randomNum > target) {
                    // 从当前位置往后移动一位
                    System.arraycopy(topn, j, topn, j + 1, n - (j + 1));
                    topn[j] = randomNum;
                    break;
                }
            }
        }
        System.out.println(Arrays.toString(topn));
    }

    public static void topn2() {
        int n = 10;
        int[] topn = new int[n];

        // 循环 n 次，模拟有这么多数据需要计算
        for (int i = 0; i < 100; i++) {
            int randomNum = RandomUtils.nextInt(100);
//            int randomNum = i;
            for (int j = 0; j < topn.length; j++) {
                int target = topn[j];
                if (randomNum >= target) {
                    topn[j] = randomNum;
                    randomNum = target;
                }
            }
        }
        System.out.println(Arrays.toString(topn));
    }
}

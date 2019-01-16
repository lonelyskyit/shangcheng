package com.leyou.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ThreadPoolDemo
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/26/026  8:23
 * @Version: 1.0
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        //ExecutorService threadPool = Executors.newFixedThreadPool(3);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        //ExecutorService threadPool = Executors.newSingleThreadExecutor();
        System.out.println("00000");
        for (int i = 0; i < 8; i++) {
            final int task =i;
//            synchronized ()
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        System.out.println(Thread.currentThread().getName()+"  线程正在循环："+"  for task of  "+task);
                   }
                }
            });
        }
        System.out.println("任务执行完成！");
       // threadPool.shutdownNow();
        //3个线程处理,10s后定时
        Executors.newScheduledThreadPool(3).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName()+"  bombing!炸了");
            }
        },10,2, TimeUnit.SECONDS);
    }
}

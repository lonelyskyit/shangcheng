package com.leyou.test;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @ClassName: CallableAndFutureDemo
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/26/026  14:40
 * @Version: 1.0
 */
public class CallableAndFutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<String> future = pool.submit(new Callable<String>() {
            @Override
            public String call() throws InterruptedException {
                Thread.sleep(2000);
                return "hello";
            }
        });
        System.out.println("future = " + future);
        //System.out.println("future = " + future.get());
        System.out.println("拿到结果："+future.get(1,TimeUnit.SECONDS));
    }


    @Test
    public void test01() throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        CompletionService<Integer> completionService=new ExecutorCompletionService(threadPool);
        for (int i = 0; i < 10; i++) {
            final int seq=i;
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Thread.sleep(new Random().nextInt(5000));
                    return seq;
                }
            });
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(completionService.take().get());
        }
    }



}

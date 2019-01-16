package com.leyou.test.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: LockDemo
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/26/026  15:03
 * @Version: 1.0
 */
public class LockDemo {
    public static void main(String[] args) {
        new LockDemo().init();
    }

    private void init() {
        Outputer outputer = new Outputer();
        Thread zhangxiaoxiao = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("zhangxiaoxiao");
                }
            }
        });
        Thread kkkkkkkkkkkkkk = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("kkkkkkkkkkkkkk");
                }
            }
        });
        zhangxiaoxiao.start();
        kkkkkkkkkkkkkk.start();
    }

    static class Outputer {
        Lock lock = new ReentrantLock();

        public void output(String name) {
            int len = name.length();
            lock.lock();
            try {
                for (int i = 0; i < len; i++) {
                    System.out.print(name.charAt(i));
                }
                System.out.println();
            } catch (Exception e) {

            }/*finally {
                lock.unlock();
            }*/
            /*synchronized (Outputer.class) {

            }*/
        }

        public synchronized void output2(String name) {
            int len = name.length();
            synchronized (Outputer.class) {
                for (int i = 0; i < len; i++) {
                    System.out.print(name.charAt(i));
                }
                System.out.println();
            }
        }
    }
}

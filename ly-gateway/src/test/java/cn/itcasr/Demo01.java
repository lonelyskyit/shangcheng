package cn.itcasr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/**
 * @ClassName: Demo01
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/28/028  14:35
 * @Version: 1.0
 */
public class Demo01 {
    //多路复用，选择器，用于注册通道
    private Selector selector;
    //缓存容器
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    private void init(int port){
        try {
            //开启多路复用器
            this.selector = Selector.open();
            //开启服务通道
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            //非阻塞
            serverChannel.configureBlocking(false);
            //绑定端口
            serverChannel.bind(new InetSocketAddress(port));
            //注册，并标记当前服务通道
            serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        while (true) {
            try {
                //阻塞方法，当至少一个通道被选中，此方法返回
                //通道是否选择，由注册到多路复用器中的通道标记决定
                this.selector.select();
                //返回已选择的通道标记集合，集合中保存的是通道的标记。相当于是通道的ID
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    //将本次要处理的通道从集合中删除，下次循环根据新的通道列表，再次执行必要的业务逻辑
                    keys.remove();
                    //通道是否有效
                    if (key.isValid()) {
                        //阻塞状态
                        try {
                            if (key.isAcceptable()) {
                                accept(key);
                            }
                        } catch (CancelledKeyException cke) {
                            //断开连接
                            key.cancel();
                        }
                        //可读状态
                        try {
                            if (key.isReadable()) {
                                read(key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            //此通道为init方法中注册到Selector的ServerSocketChannel
            ServerSocketChannel serverChannel =(ServerSocketChannel)key.channel();
            //阻塞方法，当客户端发起请求后返回
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            //设置对应客户端的通道标记状态，此通道为读取数据使用的
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {

    }















    public static void main(String[] args) {









       /* BlockingQueue<Integer> queue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<>(1);*/
        //两个队列

        //test01();
        //demo01test();
    }

    private static void test01() {
        ArrayList<String> arr = new ArrayList<>();
        arr.add("zhangsan");
        arr.add("lisi");
        arr.add("wangwu");
        arr.add("zhaoliu");
        Iterator<String> iterator = arr.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println(next);
            if (next == "zhaoliu") {
                arr.remove(next);
            }
        }
        System.out.println(arr);
    }

    public static synchronized <T> void sub(int i, BlockingQueue<Integer> queue) {
        try {
            queue.put(1);
        } catch (Exception e) {

        }
        for (int i1 = 0; i1 < 10; i1++) {
            System.out.println("sub thread sequece of "+i1+"  ,loop of "+i);
        }
    }



    private static void demo01test() {
        if (true^false){
            System.out.println(1);
        }
        System.out.println(5^2);
        System.out.println(5^1);
        System.out.println(5^3);
        System.out.println(5^5);
//        System.out.println(5^5);
//        System.out.println(5^3);
    }
}

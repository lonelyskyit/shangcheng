package com.leyou.sms.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SendTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendMsg() throws InterruptedException {
        Map<String,String> msg = new HashMap<>();
        msg.put("phone", "13600527634");
        msg.put("code", "13600527634");
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);

        Thread.sleep(10000L);
    }
}

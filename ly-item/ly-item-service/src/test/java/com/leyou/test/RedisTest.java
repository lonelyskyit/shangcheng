package com.leyou.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Test
    public void testRedis(){
        redisTemplate.opsForValue().set("hello", "world");


        String result = redisTemplate.opsForValue().get("hello");

        System.out.println("result = " + result);

        redisTemplate.opsForHash().put("user:123", "name", "Rose");
    }
}

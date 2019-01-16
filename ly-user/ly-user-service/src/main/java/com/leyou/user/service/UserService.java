package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:code:phone:";

    public Boolean checkData(String data, Integer type) {
        // 条件
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_DATA_TYPE);
        }
        // 查询并返回
        return userMapper.selectCount(user) == 0;
    }

    public void sendCode(String phone) {
        // 校验手机号
        if (!phone.matches("^1[356789]\\d{9}$")) {
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }

        // 生成验证码
        String code = NumberUtils.generateCode(6);

        // 发送mq消息到短信服务
        Map<String,String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);

        // 保存短信验证码
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        // 从redis取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        // 校验短信验证码
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        // 对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        // 写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        // 查询条件
        User record = new User();
        record.setUsername(username);

        // 查询用户
        User user = userMapper.selectOne(record);

        if (user == null) {
            // 用户名错误,返回错误信息
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        // 对密码加密
        String pwd = CodecUtils.md5Hex(password, user.getSalt());
        if (!StringUtils.equals(pwd, user.getPassword())) {
            // 密码错误,返回错误信息
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return user;
    }
}

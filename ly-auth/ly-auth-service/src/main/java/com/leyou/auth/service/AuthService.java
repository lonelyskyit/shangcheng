package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private UserClient userClient;

    public String login(String username, String password) {
        try {
            // 验证用户名密码
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            if (user == null) {
                // 用户名或密码错误
                throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }

            // 构建payload数据
            UserInfo userInfo = new UserInfo(user.getId(), username);

            // 登录成功,生成token
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());

            return token;
        } catch (Exception e){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }
}

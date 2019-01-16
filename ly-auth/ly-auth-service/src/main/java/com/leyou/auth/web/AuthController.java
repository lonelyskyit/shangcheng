package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private AuthService authService;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username, @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response
    ) {
        // 登录,获取token
        String token = authService.login(username, password);
        // 把token写到cookie
        CookieUtils.newBuilder(response).request(request).httpOnly()
                .build(prop.getCookieName(), token);
        return ResponseEntity.ok().build();
    }

    /**
     * 校验用户登录状态
     *
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request, HttpServletResponse response
    ) {
        try {
            // 解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            // 重写token, 刷新登录有效时长
            String newToken = JwtUtils.generateToken(user, prop.getPrivateKey(), prop.getExpire());

            // 写回cookie
            CookieUtils.newBuilder(response).request(request).httpOnly()
                    .build(prop.getCookieName(), newToken);
            // 返回用户
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}

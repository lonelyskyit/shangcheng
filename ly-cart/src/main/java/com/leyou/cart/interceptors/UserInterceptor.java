package com.leyou.cart.interceptors;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 获取cookie中的token
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            // 解析token,得到用户
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 传递用户
            tl.set(user);

            return true;
        } catch (Exception e) {
            // 解析失败,证明未登录,返回false,拦截请求
            return false;
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清空用户
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}

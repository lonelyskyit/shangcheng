package com.leyou.gateway.filters;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;

    /**
     * 过滤器类型
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 过滤器顺序
     * @return
     */
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    /**
     * 是否过滤
     * @return 返回TRUE,则只需run过滤逻辑,返回false则不执行
     */
    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = ctx.getRequest();
        // 获取请求路径
        String path = request.getRequestURI();

        // 判断是否允许
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (path.startsWith(allowPath)) {
                // 允许,返回true
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤逻辑
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = ctx.getRequest();
        try {
            // 获取token
            String token = CookieUtils.getCookieValue(request, jwtProp.getCookieName());
            // 解析token
            JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey());

            // TODO 校验用户权限
        } catch (Exception e){
            // 拦截请求
            ctx.setSendZuulResponse(false);
            // 指定返回状态码
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}

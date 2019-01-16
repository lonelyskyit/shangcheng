package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.config.JwtProperties;
import com.leyou.cart.interceptors.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class CartService {

    @Autowired
    private JwtProperties prop;

    private static final String KEY_PREFIX = "cart:uid:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        // 获取key
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        // 获取hashKey
        String hashKey = cart.getSkuId().toString();

        // 取出数量
        Integer num = cart.getNum();

        BoundHashOperations<String, String, String> operation = redisTemplate.boundHashOps(key);
        // 先判断当前商品在购物车中是否存在
        if (operation.hasKey(hashKey)) {
            // 存在,修改数量
            cart = JsonUtils.toBean(operation.get(hashKey), Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        // 写回redis
        operation.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        // 获取key
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        // 判断key是否存在
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, String, String> operation = redisTemplate.boundHashOps(key);
        return operation.values().stream()
                .map(s -> JsonUtils.toBean(s, Cart.class)).collect(Collectors.toList());

    }

    public void updateNum(Long skuId, Integer num) {
        // 获取key
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();

        BoundHashOperations<String, String, String> operation = redisTemplate.boundHashOps(key);
        // 获取购物车商品
        String json = operation.get(skuId.toString());
        Cart cart = JsonUtils.toBean(json, Cart.class);

        // 修改数量
        cart.setNum(num);

        // 写回redis
        operation.put(skuId.toString(), JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        // 获取key
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        // 删除
        redisTemplate.opsForHash().delete(key, skuId.toString());
    }
}

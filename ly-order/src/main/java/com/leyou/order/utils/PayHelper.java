package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.order.config.PayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.github.wxpay.sdk.WXPayConstants.FAIL;
import static com.github.wxpay.sdk.WXPayConstants.SignType;

@Slf4j
@Component
@EnableConfigurationProperties(PayConfig.class)
public class PayHelper {
    // 配置
    private PayConfig config;

    // 支付工具
    private WXPay wxPay;

    public PayHelper(PayConfig config){
        this.config  = config;
        this.wxPay = new WXPay(config, SignType.HMACSHA256);
    }

    /**
     * 扫码支付统一下单
     * @param orderId
     * @param totalPay
     * @param desc
     * @return
     */
    public String createOrder(Long orderId, Long totalPay, String desc){
        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            // 发起请求,获取结果
            Map<String, String> result = wxPay.unifiedOrder(data);

            // 校验通信标示和业务标示
            isSuccess(result);

            // 校验签名
            isValidSign(result);

            // 下单成功，获取支付链接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常失败", e);
            return null;
        }
    }

    public void isValidSign(Map<String, String> result){
        try {
            boolean boo1 = WXPayUtil.isSignatureValid(result, config.getKey(), SignType.HMACSHA256);
            boolean boo2 = WXPayUtil.isSignatureValid(result, config.getKey(), SignType.MD5);
            if(!boo1 && !boo2){
                // 签名无效
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        }catch (Exception e){
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }

    }

    public void isSuccess(Map<String, String> result) {
        String returnCode = result.get("return_code");
        if(FAIL.equals(returnCode)){
            log.error("[微信支付] 微信下单失败, 原因:{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_CREATE_ORDER_ERROR);
        }
        String resultCode = result.get("result_code");
        if(FAIL.equals(resultCode)){
            log.error("[微信支付] 微信下单失败, 错误码:{}, 错误原因:{}",
                    result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_CREATE_ORDER_ERROR);
        }
    }
}

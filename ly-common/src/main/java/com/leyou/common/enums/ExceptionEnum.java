package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {

    CATEGORY_NOT_FOUND(404, "商品分类不存在"),
    BRAND_NOT_FOUND(404, "品牌不存在"),
    SPEC_GROUP_NOT_FOUND(404, "规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404, "规格参数不存在"),
    GOODS_NOT_FOUND(404, "商品不存在"),
    GOODS_DETAIL_NOT_FOUND(404, "商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404, "商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404, "商品库存不存在"),
    BRAND_SAVE_ERROR(500, "品牌新增失败"),
    UPLOAD_FILE_ERROR(500, "文件上传失败"),
    INVALID_FILE_TYPE(400, "非法文件类型"),
    INVALID_GOODS_PARAM(400, "无效的商品参数"),
    GOODS_SAVE_ERROR(500, "新增商品失败"),
    GOODS_UPDATE_ERROR(500, "修改商品失败"),
    INVALID_DATA_TYPE(400, "无效数据类型"),
    INVALID_PHONE_NUMBER(400, "手机号格式不正确"),
    INVALID_VERIFY_CODE(400, "无效的验证码"),
    INVALID_USERNAME_PASSWORD(400, "无效的用户名或密码"),
    UNAUTHORIZED(403, "未授权"),
    CART_NOT_FOUND(404, "购物车为空"),
    CREATE_ORDER_ERROR(500, "创建订单失败"),
    CREATE_ORDER_DETAIL_ERROR(500, "创建订单详情失败"),
    CREATE_ORDER_STATUS_ERROR(500, "创建订单状态失败"),
    STOCK_NOT_ENOUGH_ERROR(500, "库存不足"),
    ORDER_NOT_FOUND(500, "订单不存在"),
    WX_CREATE_ORDER_ERROR(500, "微信下单失败"),
    INVALID_SIGN_ERROR(500, "微信下单失败"),
    INVALID_ORDER_STATUS(500, "无效的订单状态"),
    INVALID_ORDER_PARAM(500, "无效的订单参数"),
    UPDATE_ORDER_STATUS_ERROR(500, "更新订单状态失败"),
    COMMON_ERROR(0, ""),
    ;

    private int status;
    private String message;

    public int value() {
        return this.status;
    }

    public String msg() {
        return this.message;
    }

    public ExceptionEnum init(int code, String msg){
        this.status = code;
        this.message = msg;
        return this;
    };
}

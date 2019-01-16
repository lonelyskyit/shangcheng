package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        // 1 新增order表
        Order order = new Order();
        // 1.1 生成orderID, 其他order基本字段
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setPaymentType(1);
        order.setCreateTime(new Date());

        // 1.2 登录用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerRate(false);
        order.setBuyerNick(user.getUsername());
        order.setBuyerMessage("");

        // 1.3 收货人信息
        AddressDTO addr = AddressClient.findById(1L);
        order.setReceiver(addr.getName());
        order.setReceiverState(addr.getState());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverZip(addr.getZipCode());

        // 1.4 订单金额
        // 把CartDTO处理成一个map,其key实skuID,其value是数量num
        Map<Long, Integer> numMap = orderDTO.getCarts()
                .stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        List<Long> ids = new ArrayList<>(numMap.keySet());

        // 查询商品价格,运算得到
        List<Sku> skus = goodsClient.querySkuListByIds(ids);

        // 创建detail集合
        List<OrderDetail> detailList = new ArrayList<>();

        long total = 0l;
        for (Sku sku : skus) {
            Integer num = numMap.get(sku.getId());
            // 计算总金额
            total += sku.getPrice() * num;

            // 封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setTitle(sku.getTitle());
            detail.setSkuId(sku.getId());
            detail.setPrice(sku.getPrice());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setNum(num);
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detailList.add(detail);
        }
        order.setTotalPay(total);
        order.setPostFee(0L);
        // 实付金额 = 总金额 + 邮费 - 优惠
        order.setActualPay(total + order.getPostFee() - 0l);

        // 写入数据库
        int count = orderMapper.insertSelective(order);
        if(count != 1){
            // 新增失败
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 2 新增orderDetail
        count = detailMapper.insertList(detailList);
        if(count != detailList.size()){
            // 新增失败
            throw new LyException(ExceptionEnum.CREATE_ORDER_DETAIL_ERROR);
        }

        // 3 新增orderStatus
        OrderStatus status = new OrderStatus();
        status.setOrderId(orderId);
        status.setCreateTime(order.getCreateTime());
        status.setStatus(OrderStatusEnum.INIT.value());
        count = statusMapper.insertSelective(status);
        if(count != 1){
            // 新增失败
            throw new LyException(ExceptionEnum.CREATE_ORDER_STATUS_ERROR);
        }

        // 4 减库存
        List<CartDTO> cartDTOS = orderDTO.getCarts();
        goodsClient.decreaseStock(cartDTOS);
        return orderId;
    }

    public Order queryOrderById(Long orderId) {
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        List<OrderDetail> detailList = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        order.setOrderDetails(detailList);

        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String generatePayUrl(Long orderId) {
        // 查询订单
        Order order = queryOrderById(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 判断订单状态
        OrderStatus orderStatus = order.getOrderStatus();
        if(orderStatus.getStatus() != OrderStatusEnum.INIT.value()){
            // 订单已经支付或关闭, 订单状态不对
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }

        // 支付金额
        Long totalPay = /*order.getActualPay()*/ 1L;

        // 商品描述(可以把订单中的某个商品标题作为描述)
        String desc = order.getOrderDetails().get(0).getTitle();

        String url = payHelper.createOrder(orderId, totalPay, desc);
        return url;
    }

    public void handleNotify(Map<String, String> result) {
        // 1 数据校验
        payHelper.isSuccess(result);

        // 2 校验签名
        payHelper.isValidSign(result);

        // 3 校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)){
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 3.1 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        // 3.2 获取订单金额
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee != /*order.getActualPay()*/ 1){
            // 金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        // 4 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAY_UP.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = statusMapper.updateByPrimaryKeySelective(status);
        if(count != 1){
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }

        log.info("[订单回调], 订单支付成功! 订单编号:{}", orderId);
    }
}

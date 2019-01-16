package com.leyou.order.client;

import com.leyou.item.api.GoodsAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface GoodsClient extends GoodsAPI {
}

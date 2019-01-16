package com.leyou.page.client;

import com.leyou.item.api.GoodsAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface GoodsClient extends GoodsAPI {
}

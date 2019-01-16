package com.leyou.search.client;

import com.leyou.item.api.BrandAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface BrandClient extends BrandAPI {
}

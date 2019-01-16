package com.leyou.page.client;

import com.leyou.item.api.SpecificationAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface SpecificationClient extends SpecificationAPI {
}

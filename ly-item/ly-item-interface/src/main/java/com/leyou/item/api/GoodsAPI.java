package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsAPI {

    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable
    );

    @GetMapping("/spu/{spuId}")
    Spu querySpuById(@PathVariable("spuId")Long spuId);

    @GetMapping("/spu/detail/{spuId}")
    SpuDetail queryDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spuID查询sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuListBySpuId(@RequestParam("id") Long spuId);

    /**
     * 减库存
     * @param carts
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> carts);

    /**
     * 根据id查询sku集合
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuListByIds(@RequestParam("ids") List<Long> ids);
}
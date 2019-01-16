package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        // 1 分页
        PageHelper.startPage(page, rows);

        // 2 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 2.1 模糊搜索
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%"+key+"%");
        }
        // 2.2 上下架过滤
        if(saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }
        // 2.3 逻辑删除过滤
        criteria.andEqualTo("valid", true);

        // 3 默认排序
        example.setOrderByClause("last_update_time DESC");

        // 4 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        // 5 处理分类和品牌的名称
        handleCategoryAndBrandName(spus);

        // 6 返回
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>( info.getTotal() , spus);
    }

    private void handleCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
            // 处理分类名称
            List<String> names = categoryService
                    .queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setValid(true);// 默认有效
        spu.setSaleable(true);// 默认上架
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = detailMapper.insert(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 新增商品sku和库存
        saveSkuAndStock(spu);
    }

    private void saveSkuAndStock(Spu spu) {
        int count;// 新增sku
        List<Sku> skus = spu.getSkus();// 只有库存,没有id

        List<Stock> stockList = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            // 初始化stock对象
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);
        }

        // 新增stock
        count = stockMapper.insertList(stockList);
        if(count != stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 发送消息
        amqpTemplate.convertAndSend("item.insert", spu.getId());
    }

    public SpuDetail queryDetailBySpuId(Long spuId) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(spuId);
        if (detail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    public List<Sku> querySkuListBySpuId(Long spuId) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> list = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        // 获取skuID
        List<Long> ids = list.stream().map(Sku::getId).collect(Collectors.toList());
        // 查询库存
        fillSkuStock(ids, list);
        return list;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        // 获取spuId
        Long spuId = spu.getId();
        if (spuId == null) {
            throw new LyException(ExceptionEnum.INVALID_GOODS_PARAM);
        }

        Sku record = new Sku();
        record.setSpuId(spuId);
        // 查询sku信息
        List<Sku> skuList = skuMapper.select(record);

        // 判断是否存在
        if(!CollectionUtils.isEmpty(skuList)){
            // 删除sku
            int count = skuMapper.delete(record);
            if(count != skuList.size()){
                throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
            }

            // 删除库存
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }

        // 修改spu
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        // 修改detail
        count = detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        // 新增商品sku和库存
        saveSkuAndStock(spu);

        // 发送消息
        amqpTemplate.convertAndSend("item.update", spu.getId());
    }

    public Spu querySpuById(Long spuId) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 查询sku
        spu.setSkus(querySkuListBySpuId(spuId));
        // 查询detail
        spu.setSpuDetail(queryDetailBySpuId(spuId));
        return spu;
    }

    public List<Sku> querySkuListByIds(List<Long> ids) {
        // 查询sku
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        // 填充sku的库存
        fillSkuStock(ids, skus);
        return skus;
    }

    private void fillSkuStock(List<Long> ids, List<Sku> skus) {
        // 查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList) || stockList.size() != ids.size()) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        // 准备一个map,其key是sku的id,其值是stock指
        Map<Long, Integer> map = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        // 填充stock到sku
        for (Sku s : skus) {
            s.setStock(map.get(s.getId()));
        }
    }

    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if(count != 1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH_ERROR);
            }
        }
    }
}

package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        Long spuId = spu.getId();

        // 查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        // 查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 1 搜索字段
        StringBuilder all = new StringBuilder(spu.getTitle());
        for (Category category : categories) {
            all.append(" ").append(category.getName());
        }
        all.append(" ").append(brand.getName());

        // 查询sku
        List<Sku> skus = goodsClient.querySkuListBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        // 2 处理sku
        List<Map<String,Object>> skuList = new ArrayList<>();
        // 3 创建价格集合
        Set<Long> price = new HashSet<>();
        for (Sku sku : skus) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            skuList.add(map);
            // 添加价格
            price.add(sku.getPrice());
        }

        // 查询规格参数
        List<SpecParam> params = specClient.queryParams(null, spu.getCid3(), true);
        if(CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        // 查询商品详情
        SpuDetail detail = goodsClient.queryDetailBySpuId(spuId);
        if (detail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        // 通用规格
        Map<Long,String> genericSpec = JsonUtils.toMap(detail.getGenericSpec(), Long.class, String.class);
        // 特有规格参数
        Map<Long,List<String>> specialSpec =
                JsonUtils.nativeRead(detail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});

        // 4 规格参数, key是规格参数名称, 值是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        // 填充
        for (SpecParam param : params) {
            String key = param.getName();// 规格参数key
            Object value = null;
            // 判断参数是否是通用参数
            if(param.getGeneric()){
                // 通用
                value = genericSpec.get(param.getId());
                // 判断是否是数值类型
                if(param.getNumeric()){
                    // 如果是数值类型,要进行分段
                    value = chooseSegment(value.toString(), param);
                }
            }else{
                // 特有参数
                value = specialSpec.get(param.getId());
            }

            // 健壮处理
            value = value == null || StringUtils.isEmpty(value.toString()) ? "其它" : value;
            // 存入map
            specs.put(key, value);
        }

        // 创建索引库对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spu.getSubTitle());
        goods.setSpecs(specs);// 规格参数
        goods.setSkus(JsonUtils.toString(skuList));// sku集合的json格式
        goods.setPrice(price);// sku的价格集合
        goods.setAll(all.toString());// 搜索字段,包含:标题,分类,品牌,规格等
        goods.setCreateTime(spu.getCreateTime());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        // 获取查询条件
        String key = request.getKey();
        // 如果条件不存在,则返回404
        if(StringUtils.isBlank(key)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        // 查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 0 结果字段过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        // 1 查询条件
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);

        // 2 分页信息
        int page = request.getPage() - 1;
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 3 聚合
        // 3.1 聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 3.2 聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 4 搜索
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        // 5 解析结果
        // 5.1 解析分页结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> list = result.getContent();

        // 5.2 解析分类和品牌的聚合结果
        Aggregations aggs = result.getAggregations();

        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));

        // 6 处理规格参数
        List<Map<String,Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 当且仅当 分类只有1个时,我们完成规格参数的聚合
            specs = buildSpecs(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(total, totalPages, list, categories, brands, specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // 构建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 过滤条件
        Map<String, String> map = request.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs." + key + ".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecs(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs = new ArrayList<>();
        // 1 根据分类查询可搜索的规格参数
        List<SpecParam> specParams = specClient.queryParams(null, cid, true);
        // 2 准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        // 限制查询数据量
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 3 聚合条件
        for (SpecParam param : specParams) {
            // 获取规格参数名称
            String name = param.getName();// 内存,CPU频率
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        // 4 得到聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        // 5 解析结果
        Aggregations aggs = result.getAggregations();

        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets().stream()
                    .map(b -> b.getKeyAsString()).collect(Collectors.toList());
            // 准备规格参数map
            Map<String,Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", options);

            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            // 获取品牌的id集合
            List<Long> ids = terms.getBuckets().stream()
                    .map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            // 根据id查询品牌
            List<Brand> brands = brandClient.queryByIds(ids);
            return brands;
        }catch (Exception e){
            log.error("[搜索服务] 品牌查询失败!", e);
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            // 获取分类的id集合
            List<Long> ids = terms.getBuckets().stream()
                    .map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            // 查询分类
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e){
            log.error("[搜索服务] 分类查询失败!", e);
            return null;
        }
    }

    public void insertOrUpdate(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        repository.save(goods);
    }

    public void delete(Long spuId) {
        repository.deleteById(spuId);
    }
}

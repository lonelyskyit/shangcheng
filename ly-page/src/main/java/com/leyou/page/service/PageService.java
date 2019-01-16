package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModels(Long spuId) {

        // 准备模型数据
        Map<String, Object> models = new HashMap<>();

        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询detail
        SpuDetail detail = spu.getSpuDetail();
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        // 查询规格参数
        List<SpecGroup> specs = specClient.querySpecs(spu.getCid3());

        // 填充模型数据
        models.put("title", spu.getTitle());
        models.put("subTitle", spu.getSubTitle());
        models.put("categories", categories);
        models.put("brand", brand);
        models.put("detail", detail);
        models.put("skus", skus);
        models.put("specs", specs);

        return models;
    }

    public void createHtml(Long spuId) {
        // 上下文
        Context context = new Context();
        context.setVariables(loadModels(spuId));
        // 准备文件
        File dest = deleteHtml(spuId);

        // 创建流,关联目标文件
        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("[静态页服务] 创建静态页失败!, spuId: {}", spuId, e);
        }
    }

    public File getDestFile(Long spuId) {
        // 寻找目录
        File dir = new File("/Users/zhanghuyi/lesson/heima41/upload");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, spuId + ".html");
    }

    public File deleteHtml(Long spuId) {
        File dest = getDestFile(spuId);
        if(dest.exists()){
            dest.delete();
        }
        return dest;
    }
}

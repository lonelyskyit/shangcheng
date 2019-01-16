package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void loadData(){
        // 创建索引
        template.createIndex(Goods.class);
        // 添加映射
        template.putMapping(Goods.class);
        // 查询spu
        int page = 1, rows = 100;
        do {
            try{
                PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, null, true);

                List<Spu> list = result.getItems();
                if(CollectionUtils.isEmpty(list)){
                    break;
                }
                List<Goods> goodsList = list.stream()
                        .map(searchService::buildGoods).collect(Collectors.toList());

                // 存入索引库
                goodsRepository.saveAll(goodsList);

                page++;
            } catch (Exception e){
                break;
            }
        }while (true);
    }
}
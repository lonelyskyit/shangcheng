package com.leyou.page.web;

import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Slf4j
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String hello(Model model, @PathVariable("id") Long spuId){
        // 查询模型数据
        Map<String, Object> models = pageService.loadModels(spuId);
        // 填充页面需要的model数据
        model.addAllAttributes(models);

        // TODO 创建页面
        return "item";
    }
}

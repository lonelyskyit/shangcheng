package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specService;

    /**
     * 根据cid查询规格组
     * @param cid 商品分类id
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specService.queryGroupByCid(cid));
    }

    /**
     * 查询规格参数
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否进行搜索过滤
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid", required = false)Long gid,
            @RequestParam(value = "cid", required = false)Long cid,
            @RequestParam(value = "searching", required = false)Boolean searching
    ){
        return ResponseEntity.ok(specService.queryParams(gid, cid, searching));
    }

    /**
     * 根据cid查询规格组及组内参数
     * @param cid
     * @return
     */
    @GetMapping("list/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecs(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(specService.querySpecsByCid(cid));
    }
}

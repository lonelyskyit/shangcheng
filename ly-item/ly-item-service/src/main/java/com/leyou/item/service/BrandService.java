package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryBrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.CategoryBrand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤条件
        Example example = new Example(Brand.class);
        if (StringUtils.isNoneBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        // 排序
        if (StringUtils.isNoneBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);// id desc
        }
        // 查询
        List<Brand> brands = brandMapper.selectByExample(example);

        // 判断是否为空
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        // 解析分页结果
        PageInfo<Brand> info = new PageInfo<>(brands);

        // 返回
        return new PageResult<>(info.getTotal(), info.getList());
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // TODO 数据校验
        brand.setId(null);
        // 新增品牌
        int count = brandMapper.insert(brand);
        if (count != 1) {
            // 新增失败,抛出异常
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        // 中间表
        List<CategoryBrand> cbList = cids.stream()
                .map(cid -> new CategoryBrand(cid, brand.getId()))
                .collect(Collectors.toList());

        // 批量新增
        count = categoryBrandMapper.insertList(cbList);
        if (count != cbList.size()) {
            // 新增失败
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
    }

    public Brand queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        // 判断是否为空
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        // 判断是否为空
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }
}

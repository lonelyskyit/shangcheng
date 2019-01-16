package com.leyou.item.mapper;

import com.leyou.item.pojo.CategoryBrand;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface CategoryBrandMapper extends Mapper<CategoryBrand>, InsertListMapper<CategoryBrand> {
}

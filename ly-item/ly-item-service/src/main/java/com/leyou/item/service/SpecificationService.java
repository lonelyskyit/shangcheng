package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = groupMapper.select(group);
        if(CollectionUtils.isEmpty(list)){
            // 没查到
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParams(Long gid, Long cid, Boolean searching) {
        // 查询条件
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);

        // 查询
        List<SpecParam> list = paramMapper.select(param);
        if(CollectionUtils.isEmpty(list)){
            // 没查到
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> groups = queryGroupByCid(cid);
        // 查询所有参数,并且变成一个map,其key是groupId,其值是组内所有参数的集合
        Map<Long, List<SpecParam>> map = queryParams(null, cid, null).stream()
                .collect(Collectors.groupingBy(SpecParam::getGroupId));

        for (SpecGroup group : groups) {
            group.setParams(map.get(group.getId()));
        }
        return groups;
    }
}

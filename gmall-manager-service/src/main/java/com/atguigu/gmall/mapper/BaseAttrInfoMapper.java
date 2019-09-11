package com.atguigu.gmall.mapper;

import com.atguigu.gmall.entity.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    public List<BaseAttrInfo> getBaseAttrList(String catalog3Id);
}

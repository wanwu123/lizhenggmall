package com.atguigu.gmall.mapper;

import com.atguigu.gmall.entity.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    public List<BaseAttrInfo> getBaseAttrList(String catalog3Id);

    public List<BaseAttrInfo> getBaseAttrInfoListByValueIds(@Param("valueIds") String valueIds);
}

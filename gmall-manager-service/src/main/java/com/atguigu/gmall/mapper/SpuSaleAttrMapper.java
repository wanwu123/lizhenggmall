package com.atguigu.gmall.mapper;

import com.atguigu.gmall.entity.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
}

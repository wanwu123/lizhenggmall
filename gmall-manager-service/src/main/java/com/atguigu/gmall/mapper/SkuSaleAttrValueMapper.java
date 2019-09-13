package com.atguigu.gmall.mapper;

import com.atguigu.gmall.entity.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<Map> getSaleAttrValuesByspuId(String spuId);
}

package com.atguigu.gmall.mapper;

import com.atguigu.gmall.entity.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);
    List<SpuSaleAttr> getSpuSaleAttrListCheck(@Param("skuId") String skuId,@Param("spuId") String spuId);
}

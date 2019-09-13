package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ManagerService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    void save(BaseAttrInfo baseAttrInfo);


    List<BaseAttrValue> getAttrValueList(String attrId);

    BaseAttrInfo getBaseInfo(String attrId);

    public void saveSpuInfo(SpuInfo spuInfo);
    List<BaseSaleAttr> getBaseSaleAttrList();


    public List<SpuInfo> selectSpulist(String catalog3Id);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getspuSaleAttrList(String spuId);


    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    void saveSkuInfo(SkuInfo skuForm);

    public SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheck(String skuId,String spuId);

    public Map getSaleAttrValuesByspuId(String spuId);
}

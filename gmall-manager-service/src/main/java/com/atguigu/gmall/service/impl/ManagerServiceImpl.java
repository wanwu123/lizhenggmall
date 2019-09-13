package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.entity.*;
import com.atguigu.gmall.mapper.*;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.EscapedErrors;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public Map getSaleAttrValuesByspuId(String spuId) {
        List<Map> saleAttrValuesByspuId = skuSaleAttrValueMapper.getSaleAttrValuesByspuId(spuId);
        Map<Object, Object> objectObjectHashMap = new HashMap<>();
        for (Map map : saleAttrValuesByspuId) {
            String sku_id =map.get("sku_id")+"";
            String ids =map.get("value_id")+"";
            objectObjectHashMap.put(ids,sku_id);
        }
        return objectObjectHashMap;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheck(String skuId, String spuId) {
        List<SpuSaleAttr> spuSaleAttrListCheck = spuSaleAttrMapper.getSpuSaleAttrListCheck(skuId, spuId);
        return spuSaleAttrListCheck;

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.set("k1","v1");
        jedis.close();
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImages);
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> select = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(select);
        return skuInfo;
    }

    @Override
    public void saveSkuInfo(SkuInfo skuForm) {
        if (skuForm.getId()==null || skuForm.getId().length() == 0 ) {
            skuForm.setId(null);
            skuInfoMapper.insertSelective(skuForm);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuForm);
        }
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuForm.getId());
        skuImageMapper.delete(skuImage);
        List<SkuImage> skuImageList = skuForm.getSkuImageList();
        if (skuImageList!= null && skuImageList.size()>0 ){
            for (SkuImage image : skuImageList) {
                if(image.getId()!= null && image.getId().length()>0){
                    image.setId(null);
                }
                image.setSkuId(skuForm.getId());
                skuImageMapper.insertSelective(image);
            }
        }
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setAttrId(skuForm.getId());
        skuAttrValueMapper.delete(skuAttrValue);
        List<SkuAttrValue> skuAttrValueList = skuForm.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size()>0){
            for (SkuAttrValue attrValue : skuAttrValueList) {
                if (attrValue.getId() != null && attrValue.getId().length()!=0){
                    attrValue.setId(null);
                }
                attrValue.setSkuId(skuForm.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setId(skuForm.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuForm.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList !=null && skuSaleAttrValueList.size() !=0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                if (saleAttrValue.getId() != null && saleAttrValue.getId().length() != 0 ){
                    saleAttrValue.setId(null);
                }
                saleAttrValue.setSkuId(skuForm.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrList(spuId);
    }

    @Override
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> select = spuSaleAttrMapper.select(spuSaleAttr);
        return select;
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }

    @Override
    public List<SpuInfo> selectSpulist(String catalog3Id) {


        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 什么情况下是保存，什么情况下是更新 spuInfo
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            //保存数据
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }

        //  spuImage 图片列表 先删除，在新增
        //  delete from spuImage where spuId =?
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        // 保存数据，先获取数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            // 循环遍历
            for (SpuImage image : spuImageList) {
                image.setId(null);
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(image);
            }
        }
        // 销售属性 删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        // 获取数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            // 循环遍历
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                saleAttr.setId(null);
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                // 添加销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    // 循环遍历
                    for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        saleAttrValue.setId(null);
                        saleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                    }
                }

            }
        }
//        spuInfoMapper.insertSelective(spuInfo);
//        //图片
//        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
//        for (SpuImage spuImage : spuImageList) {
//            spuImage.setSpuId(spuInfo.getId());
//            spuImageMapper.insertSelective(spuImage);
//        }
//        //spu信息
//        //销售属性
//        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
//        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
//            spuSaleAttr.setSpuId(spuInfo.getId());
//            spuSaleAttrMapper.insertSelective(spuSaleAttr);
//            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
//            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
//                spuSaleAttrValue.setSpuId(spuInfo.getId());
//                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
//            }
//
//        }

    }
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    @Override
    public BaseAttrInfo getBaseInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectByExample(example);
        baseAttrInfo.setAttrValueList(baseAttrValues);
        return baseAttrInfo;
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectByExample(example);
        return baseAttrValues;
    }

    @Override
    public void save(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setId(null);
                String id = baseAttrInfo.getId();
                baseAttrValue.setAttrId(id);
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

    }

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1s = baseCatalog1Mapper.selectAll();
        return baseCatalog1s;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> select = baseCatalog2Mapper.select(baseCatalog2);
        return select;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> select = baseCatalog3Mapper.select(baseCatalog3);
        return select;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
       /* BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(baseAttrInfo);
        for (BaseAttrInfo attrInfo : select) {
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> select1 = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(select1);
        }*/
        List<BaseAttrInfo> baseAttrList = baseAttrInfoMapper.getBaseAttrList(catalog3Id);
        return baseAttrList;
    }

}

package com.atguigu.gmall.managerweb.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.entity.*;
import com.atguigu.gamll.service.ListService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ManagerController {


    @Reference
    private ManagerService managerService;

    @Reference
    private ListService listService;
    @RequestMapping(value = "getCatalog1",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseCatalog1> getCatalog1List(){
        List<BaseCatalog1> catalog1 = managerService.getCatalog1();
        return catalog1;

    }

    @RequestMapping(value = "getCatalog2",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam("catalog1Id")String catalog1Id){
        List<BaseCatalog2> catalog2 = managerService.getCatalog2(catalog1Id);
        return catalog2;
    }
    @RequestMapping(value = "getCatalog3",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(@RequestParam("catalog2Id")String catalog2Id){
        List<BaseCatalog3> catalog3 = managerService.getCatalog3(catalog2Id);
        return catalog3;
    }
    @RequestMapping(value ="attrInfoList",method = RequestMethod.GET)
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(@RequestParam("catalog3Id")String catalog3Id){
        List<BaseAttrInfo> attrList = managerService.getAttrList(catalog3Id);
        return attrList;
    }


    @RequestMapping(value = "saveAttrInfo",method = RequestMethod.POST)
    @Transactional
    public String saveAttrInfo(@RequestBody  BaseAttrInfo baseAttrInfo) {
        managerService.save(baseAttrInfo);
        return "success";
    }
    @RequestMapping(value = "getAttrValueList",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(@RequestParam("attrId")String attrId) {

        BaseAttrInfo baseAttrInfo = (BaseAttrInfo) managerService.getBaseInfo(attrId);
        List<BaseAttrValue> baseAttrList = baseAttrInfo.getAttrValueList();
        return baseAttrList;
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return  managerService.getBaseSaleAttrList();
    }
    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);
        return "success";
    }
    @RequestMapping(value = "spuList",method = RequestMethod.GET)
    @ResponseBody
    public List<SpuInfo> selectSpulist(String catalog3Id){
        List<SpuInfo> spuInfos =  managerService.selectSpulist(catalog3Id);
        return spuInfos;
    }

    @RequestMapping(value = "spuSaleAttrList",method =RequestMethod.GET)
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        List<SpuSaleAttr> list =  managerService.getSpuSaleAttrList(spuId);
        return list;
    }

    public String onSaleByspuId(String spuId){
        Map saleAttrValuesByspuId = managerService.getSaleAttrValuesByspuId(spuId);
        return null;     
    }
    @RequestMapping(value = "onSale",method =RequestMethod.POST)
    @ResponseBody
    public String onSale(@RequestParam("skuId") String skuId){
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuLsInfo(skuLsInfo);
        return "success";
    }
}

package com.atguigu.gmall.managerweb.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.entity.SkuInfo;
import com.atguigu.gmall.entity.SpuImage;
import com.atguigu.gmall.entity.SpuSaleAttr;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin
public class SkuController {

    @Reference
    private ManagerService managerService;
    @RequestMapping(value = "spuImageList",method = RequestMethod.GET)
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        return   managerService.getSpuImageList(spuId);
    }
//    @RequestMapping(value = "spuSaleAttrList",method = RequestMethod.GET)
//    @ResponseBody
//    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
//       return managerService.getspuSaleAttrList(spuId);
//    }
    @RequestMapping(value = "saveSkuInfo",method = RequestMethod.POST)
    public String saveSkuInfo(@RequestBody SkuInfo skuForm){
        managerService.saveSkuInfo(skuForm);
        return "success";
    }
}

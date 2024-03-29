package com.atguigu.gmall.itemweb.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.ListService;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.entity.SkuInfo;
import com.atguigu.gmall.entity.SpuSaleAttr;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Controller
public class itemController {
    @Reference
    private ListService listService;

    @Reference
    private ManagerService managerService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, Model model, HttpServletRequest request){
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        if (skuInfo == null){
            return null;
        }
        String spuId = skuInfo.getSpuId();
        List<SpuSaleAttr> spuSaleAttrListCheck = managerService.getSpuSaleAttrListCheck(skuId, spuId);
        model.addAttribute("skuInfo",skuInfo);
        model.addAttribute("spuSaleAttrListCheck",spuSaleAttrListCheck);
        Map saleAttrValuesByspuId = managerService.getSaleAttrValuesByspuId(spuId);
        String s = JSON.toJSONString(saleAttrValuesByspuId);
        model.addAttribute("valuesSkuJson",s);
        listService.incrHotScore(skuId);
        String userId = (String) request.getAttribute("userId");
        return "item";
    }
}

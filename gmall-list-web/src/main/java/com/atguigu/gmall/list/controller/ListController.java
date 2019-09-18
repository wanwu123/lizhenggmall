package com.atguigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.ListService;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.entity.BaseAttrInfo;
import com.atguigu.gmall.entity.SkuLsParams;
import com.atguigu.gmall.entity.SkuLsResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ListController {
    @Reference
    private ManagerService managerService;
    @Reference
    private ListService listService;
    @GetMapping("list.html")
//    @ResponseBody
    public String getList(SkuLsParams skuLsParams,Model model){
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
//        return JSON.toJSONString(search);
        model.addAttribute("skuLsResult",skuLsResult);
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = managerService.getAttrList(attrValueIdList);
        model.addAttribute("attrList",attrList);
        return "list";
    }


}

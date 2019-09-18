package com.atguigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.ListService;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.entity.BaseAttrInfo;
import com.atguigu.gmall.entity.BaseAttrValue;
import com.atguigu.gmall.entity.SkuLsParams;
import com.atguigu.gmall.entity.SkuLsResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
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
        String paramUrl = makeParamUrl(skuLsParams);
        model.addAttribute("paramUrl",paramUrl);
        List<BaseAttrValue> selectedValueList = new ArrayList<>();
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String checkedValue = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals(checkedValue)) {//如果清单属性和已选择属性相同则删除
                            iterator.remove();
                            String urlparam = makeParamUrl(skuLsParams, checkedValue);
                            baseAttrValue.setUrlParam(urlparam);
                            selectedValueList.add(baseAttrValue);
                        }
                    }
                }
            }
        }
        String keyword = skuLsParams.getKeyword();
        model.addAttribute("keyword",keyword);
        model.addAttribute("selectedValueList",selectedValueList);
        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        return "list";
    }
    public String makeParamUrl(SkuLsParams skuLsParams,String... excludeValueIds){
        String paramUrl = "";
        if (skuLsParams.getKeyword()!=null){
            paramUrl += "keyword="+skuLsParams.getKeyword();
        }else  if (skuLsParams.getCatalog3Id() != null){
            paramUrl +="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueIds!=null && excludeValueIds.length>0){//需要排除
                    String excludeValueId = excludeValueIds[0];
                    if (valueId.equals(excludeValueId)){
                        continue;
                    }
                }
                if (paramUrl.length()>0){
                    paramUrl+="&";
                }
                paramUrl+="valueId="+valueId;
            }
        }
        return paramUrl;
    }


}

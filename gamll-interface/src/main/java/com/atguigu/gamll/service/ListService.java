package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.SkuLsInfo;
import com.atguigu.gmall.entity.SkuLsParams;
import com.atguigu.gmall.entity.SkuLsResult;

public interface ListService {

    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}

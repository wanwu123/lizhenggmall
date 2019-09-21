package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.CartInfo;

public interface CartService {

    CartInfo addCart(String userId, String skuId, Integer num);
}

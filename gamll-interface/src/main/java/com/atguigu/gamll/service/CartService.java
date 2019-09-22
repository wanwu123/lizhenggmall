package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.CartInfo;

import java.util.List;

public interface CartService {

    CartInfo addCart(String userId, String skuId, Integer num);
    List<CartInfo> cartList(String userId);

    List<CartInfo> mergeCartList(String userId, String tmpId);

    void checkCart(String userId, String skuId, String isChecked);

    List<CartInfo> getCheckedCartList(String userId);
}

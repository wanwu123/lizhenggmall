package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.OrderInfo;

public interface OrderService {

    void saveOrder(OrderInfo orderInfo);
    String getToken(String userId);

    Boolean verifyToken(String userId,String token);
}

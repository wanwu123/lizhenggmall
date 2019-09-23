package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.OrderInfo;

public interface OrderService {

    String saveOrder(OrderInfo orderInfo);
    String getToken(String userId);

    Boolean verifyToken(String userId,String token);

    OrderInfo getOrderInfo(String orderId);
}

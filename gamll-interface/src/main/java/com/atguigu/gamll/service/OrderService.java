package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.enums.ProcessStatus;

public interface OrderService {

    String saveOrder(OrderInfo orderInfo);
    String getToken(String userId);

    Boolean verifyToken(String userId,String token);

    OrderInfo getOrderInfo(String orderId);

    void updateOrederStatus(String orderId, ProcessStatus paid,OrderInfo... orderInfo);
}

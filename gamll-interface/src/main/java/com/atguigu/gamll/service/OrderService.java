package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    String saveOrder(OrderInfo orderInfo);
    String getToken(String userId);

    Boolean verifyToken(String userId,String token);

    OrderInfo getOrderInfo(String orderId);
    List<Integer> checkExpiredCoupon();
    void handleExpiredCoupon(Integer id);

    void updateOrederStatus(String orderId, ProcessStatus paid,OrderInfo... orderInfo);

    List<Map> orderSplit(String orderId, String wareSkuMap);

    String initWareOrder(String orderId);
}

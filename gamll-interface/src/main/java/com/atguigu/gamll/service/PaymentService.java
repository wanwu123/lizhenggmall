package com.atguigu.gamll.service;

import com.atguigu.gmall.entity.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo);

    void sendPaymentToOrder(String orderId,String result);

    void sendDelayPaymentResult(String outTradeNo,Long delaySec,Integer checkCount);
}

package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gamll.service.PaymentService;
import com.atguigu.gmall.entity.PaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PaymentInfoServiceImpl implements PaymentService{
    @Autowired
    private PaymentMapper paymentMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }
}

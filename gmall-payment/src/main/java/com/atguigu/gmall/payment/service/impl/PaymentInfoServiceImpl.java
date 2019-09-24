package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gamll.service.PaymentService;
import com.atguigu.gmall.entity.PaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentInfoServiceImpl implements PaymentService{
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentMapper.updateByExampleSelective(paymentInfo,example);

    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        PaymentInfo paymentInfo1 = paymentMapper.selectOne(paymentInfo);
        return paymentInfo1;
    }

    @Autowired
    private PaymentMapper paymentMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }
}

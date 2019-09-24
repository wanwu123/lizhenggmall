package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gamll.service.PaymentService;
import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.PaymentInfo;
import com.atguigu.gmall.entity.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;

@Controller
public class PaymentController {
    @Reference
    private PaymentService paymentService;
    @Reference
    private OrderService orderService;
    @Autowired
    private AlipayClient alipayClient;

    @GetMapping("index")
    @LoginRequire
    public  String index(HttpServletRequest request, Model model){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId",orderId);
        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }
    @PostMapping("alipay/submit")
    public String alipaySubmit(String orderId, HttpServletResponse response){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        long date = System.currentTimeMillis();
        String outTradeNo = "ATGUIGU"+date+orderInfo.getId();
        orderInfo.setOutTradeNo(outTradeNo);
        String productcode = "FAST_INSTANT_TRADE_PAY";
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        String subject = orderInfo.genSubject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",outTradeNo);
        jsonObject.put("product_code",productcode);
        jsonObject.put("total_amount",totalAmount);
        jsonObject.put("subject",subject);
        alipayTradePagePayRequest.setBizContent(jsonObject.toJSONString());
        String AlipayHtml = null;
        try {
            AlipayHtml = alipayClient.execute(alipayTradePagePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        //保存
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus( PaymentStatus.UNPAID);
        paymentService.savePaymentInfo(paymentInfo);
        return AlipayHtml;
    }


}

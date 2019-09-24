package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    private PaymentService paymentService;
    @Reference
    private OrderService orderService;
    @Autowired
    private AlipayClient alipayClient;

    @GetMapping("index")
//    @LoginRequire
    public  String index(HttpServletRequest request, Model model){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId",orderId);
        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }
    @PostMapping("alipay/submit")
    @ResponseBody
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
            AlipayHtml = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();
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
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public  String paymentNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request){
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "UTF-8", AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (!flag){
            return "fail";
        }
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            String total_amount = paramMap.get("total_amount");
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfo1 = paymentService.getPaymentInfo(paymentInfo);
            if (paymentInfo1.getPaymentStatus()==PaymentStatus.PAID || paymentInfo1.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setAlipayTradeNo(paramMap.get("trade_no"));
                paymentInfo.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfo);
                return "success";
            }

        }
        return "fail";
    }

    @GetMapping("alipay/callback/return")
    @ResponseBody
    public String alipayReturn(){
        return "return了";
    }
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId) throws AlipayApiException {
        AlipayTradeRefundRequest  alipayTradePagePayRequest = new AlipayTradeRefundRequest();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo paymentInfo1 = paymentService.getPaymentInfo(paymentInfo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",paymentInfo1.getOutTradeNo());
        jsonObject.put("refund_amount",paymentInfo1.getTotalAmount());
        alipayTradePagePayRequest.setBizContent(jsonObject.toJSONString());
        AlipayTradeRefundResponse  response = alipayClient.execute(alipayTradePagePayRequest);
        if(response.isSuccess()){
            System.out.println("调用成功");
            System.out.println("业务退款成功");
            PaymentInfo paymentInfoUpdate =  new PaymentInfo();
            paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAY_REFOUND);
            paymentService.updatePaymentInfo(paymentInfo1.getOutTradeNo(),paymentInfo);
            return "success";
        } else {
            System.out.println("调用失败");
            return "fail";
        }
    }
}

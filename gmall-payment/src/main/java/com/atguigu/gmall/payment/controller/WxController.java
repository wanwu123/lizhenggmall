package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gamll.service.PaymentService;
import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.PaymentInfo;
import com.atguigu.gmall.entity.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.WxConfig;
import com.atguigu.gmall.payment.util.HttpClient;
import com.atguigu.gmall.payment.util.StreamUtil;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WxController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @PostMapping("/wx/submit")
    public Map wxSubmit(String orderId) throws Exception {
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        Map paramMap = new HashMap();
        WxConfig wxConfig = new WxConfig();

        paramMap.put("appid",WxConfig.APPID);
        paramMap.put("mch_id",WxConfig.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body", orderInfo.genSubject());
        long date = System.currentTimeMillis();
        String outTradeNo = "ATGUIGU"+date+orderInfo.getId();
        paramMap.put("out_trade_no",outTradeNo);
        paramMap.put("spbill_create_ip","127.0.0.1");
        paramMap.put("total_fee",orderInfo.getTotalAmount().multiply(new BigDecimal(100)).toBigInteger().toString());
        paramMap.put("notify_url","http://lizhengwanwu.free.idcfengye.com/wx/callback/notify");
        paramMap.put("trade_type","NATIVE");
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, WxConfig.PARTNERKEY);

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setXmlParam(xmlParam);
        httpClient.post();
        String content = httpClient.getContent();//得到返回结果的xml
        //XML解析为Map
        Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
        if (resultMap.get("code_url")!=null){
            String code_url = resultMap.get("code_url");
            return resultMap;
        }else {
            System.out.println(resultMap.get("return_code"));
            System.out.println(resultMap.get("return_msg"));
            return null;
        }
    }

    @PostMapping("wx/callback/notify")
    public String wxNotify(HttpServletRequest request,HttpServletResponse response ) throws Exception {
        //获得值
        ServletInputStream inputStream = request.getInputStream();
        String XmlString = StreamUtil.inputStream2String(inputStream, "UTF-8");
        //验签
        if (WXPayUtil.isSignatureValid(XmlString,WxConfig.PARTNERKEY)){
            Map<String, String> stringStringMap = WXPayUtil.xmlToMap(XmlString);
            //判断状态
            String result_code = stringStringMap.get("result_code");
            if (result_code !=null && result_code.equals("SUCCESS")){
                //更新支付状态
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setAlipayTradeNo(stringStringMap.get("trade_no"));
                paymentInfo.setCallbackContent(stringStringMap.toString());
                paymentService.updatePaymentInfo(stringStringMap.get("out_trade_no"),paymentInfo);
                //准备返回值xml
                Map<String, String> returnXml = new HashMap<>();
                returnXml.put("return_code","SUCCESS");
                returnXml.put("return_msg","OK");
                String mapToXmlString = WXPayUtil.mapToXml(returnXml);
                response.setContentType("text/xml");
                System.out.println("交易编号："+stringStringMap.get("out_trade_no")+"支付成功！");
                return mapToXmlString;
            }else {
                System.out.println(stringStringMap.get("return_code")+"---"+stringStringMap.get("return_msg"));
            }

        }   
        return null;
    }
}

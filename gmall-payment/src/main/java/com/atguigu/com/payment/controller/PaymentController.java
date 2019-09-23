package com.atguigu.com.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.entity.OrderInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PaymentController {
    @Reference
    private OrderService orderService;


    @GetMapping("index")
//    @LoginRequire
    public  String index(HttpServletRequest request, Model model){
//        String orderId = request.getParameter("orderId");
//        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
//        model.addAttribute("orderId",orderId);
//        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }
    @GetMapping("AAA")
    @ResponseBody
    public String aaa(){
        return "Aaa";
    }
}

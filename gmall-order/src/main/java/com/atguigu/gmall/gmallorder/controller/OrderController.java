package com.atguigu.gmall.gmallorder.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.CartService;
import com.atguigu.gamll.service.UserService;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.entity.CartInfo;
import com.atguigu.gmall.entity.UserAddress;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;


    @GetMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String trade(HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");
        //用户地址
        List<UserAddress> address = userService.getAddress(userId);
        request.setAttribute("address",address);
        //清单
        List<CartInfo> cartList = cartService.getCheckedCartList(userId);
        BigDecimal totalAoumt = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            BigDecimal skuPrice = cartInfo.getSkuPrice();
            Integer skuNum = cartInfo.getSkuNum();
            BigDecimal bigDecimal = new BigDecimal(skuNum);
            BigDecimal multiply = skuPrice.multiply(bigDecimal);
            totalAoumt = totalAoumt.add(multiply);
        }
        request.setAttribute("cartList",cartList);

        request.setAttribute("totalAoumt",totalAoumt);
        return "trade";
    }
}

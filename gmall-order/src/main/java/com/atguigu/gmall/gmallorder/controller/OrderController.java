package com.atguigu.gmall.gmallorder.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.CartService;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gamll.service.UserService;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.entity.*;
import com.atguigu.gmall.entity.enums.OrderStatus;
import com.atguigu.gmall.entity.enums.ProcessStatus;
import org.apache.catalina.Manager;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;
    @Reference
    private ManagerService managerService;
    @Reference
    private OrderService orderService;
    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(),15));
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = managerService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());
        }
        
        orderService.saveOrder(orderInfo);
        return "redirect://payment.gmall.com/index";
    }

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

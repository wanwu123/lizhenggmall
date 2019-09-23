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
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.catalina.Manager;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.DateUtil;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

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
        String tradeNo = (String)request.getParameter("tradeNo");
        Boolean aBoolean = orderService.verifyToken(userId, tradeNo);
        if(!aBoolean){
            request.setAttribute("errMsg","页面已失效，请重新结算！");
            return  "tradeFail";
        }
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
            if (!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){
                request.setAttribute("errMsg","商品价格已发生变动请重新加入购物车！");
                return  "tradeFail";
            }
        }
        List<OrderDetail> relist = Collections.synchronizedList(new ArrayList<>());
        Stream<CompletableFuture<String>> completableFutureStream = orderDetailList.stream().map(orderDetail -> CompletableFuture.supplyAsync(() -> checkSkuNum(orderDetail)).whenComplete((ifPaas, ex) -> {
            if (ifPaas.equals("0")) {
                relist.add(orderDetail);
            }
        }));
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
        if (relist!=null && relist.size()>0){
            StringBuffer stringBuffer = new StringBuffer();
            for (OrderDetail orderDetail : relist) {
                stringBuffer.append("商品:"+orderDetail.getSkuName()+"库存不足");
            }
            request.setAttribute("errMsg",stringBuffer.toString());
            return  "tradeFail";
        }
        String orderId = orderService.saveOrder(orderInfo);
        //清空购物车
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

    public String checkSkuNum(OrderDetail orderDetail){
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
        return result;
    }
    @Test
    public void test1(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
//        List reList = new CopyOnWriteArrayList();
        List reList = Collections.synchronizedList(new ArrayList<>());
        /*Integer[] objects = list.stream().map(num -> {
            if (checkNum(num)) {
                reList.add(num);
            }
            return num;
        }).toArray(Integer[]::new);*/
        Stream<CompletableFuture<Boolean>> completableFutureStream = list.stream().map(num ->
                CompletableFuture.supplyAsync(() ->
                        checkNum(num)
        ).whenComplete((ifPass, ex) -> {
            if (ifPass) {
                reList.add(num);
            }
        }));
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
        System.out.println(reList);
    }
    private Boolean checkNum(Integer num){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (num%3==0){
            return true;
        }else{
            return false;
        }
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
        String token = orderService.getToken(userId);
        request.setAttribute("tradeNo",token);
        request.setAttribute("cartList",cartList);

        request.setAttribute("totalAoumt",totalAoumt);
        return "trade";
    }
}

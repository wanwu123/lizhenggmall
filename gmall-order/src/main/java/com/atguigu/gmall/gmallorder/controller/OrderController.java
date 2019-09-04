package com.atguigu.gmall.gmallorder.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.UserService;
import com.atguigu.gmall.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Reference
    private UserService userService;

    @GetMapping("trade")
    public UserInfo trade(@RequestParam("userId") String userId){
        UserInfo userById = userService.getUserById(userId);
        return userById;
    }
}

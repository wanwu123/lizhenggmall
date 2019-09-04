package com.atguigu.gmall.user.controller;



import com.atguigu.gamll.service.UserService;
import com.atguigu.gmall.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("getAllList")
    private List<UserInfo> getAllList(){
        return userService.getUserInfoListAll();
    }
    @PostMapping("addUser")
    public String addUser(UserInfo userInfo){
        userService.addUser(userInfo);
        return "success";
    }
    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo){
        userService.updateUser(userInfo);
        return "success";
    }

    @PostMapping("updateUserByName")
    public String updateUserByName(String name,UserInfo userInfo){
        userService.updateUserByName(name,userInfo);
        return "success";
    }
    @PostMapping("delUser")
    public String delUser(UserInfo userInfo){
        userService.delUser(userInfo);
        return "success";
    }
    @GetMapping("getUserById")
    public UserInfo getUserById(@RequestParam("id") String id){
        UserInfo user = userService.getUserById(id);
        return user;
    }

}

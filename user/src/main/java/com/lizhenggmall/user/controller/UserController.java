package com.lizhenggmall.user.controller;


import com.lizhenggmall.user.entity.User;
import com.lizhenggmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("getAll")
    private List<User> getAllList(){
        return userService.getUserInfoListAll();
    }
    @PostMapping("addUser")
    public String addUser(User userInfo){
        userService.addUser(userInfo);
        return "success";
    }
    @PostMapping("updateUser")
    public String updateUser(User userInfo){
        userService.updateUser(userInfo);
        return "success";
    }

    @PostMapping("updateUserByName")
    public String updateUserByName(String name,User userInfo){
        userService.updateUserByName(name,userInfo);
        return "success";
    }
    @PostMapping("delUser")
    public String delUser(User userInfo){
        userService.delUser(userInfo);
        return "success";
    }
    @GetMapping("getUserById")
    public User getUserById(String id){
        User user = userService.getUserById(id);
        return user;
    }

}

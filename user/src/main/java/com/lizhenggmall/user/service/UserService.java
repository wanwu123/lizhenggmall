package com.lizhenggmall.user.service;

import com.lizhenggmall.user.entity.User;

import java.util.List;

public interface UserService {
    List<User> getUserInfoListAll();

    void addUser(User userInfo);

    void updateUser(User userInfo);

    void updateUserByName(String name,User userInfo);

    void delUser(User userInfo);

    User getUserById(String id);
}

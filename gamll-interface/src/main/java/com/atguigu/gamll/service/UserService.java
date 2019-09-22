package com.atguigu.gamll.service;


import com.atguigu.gmall.entity.UserAddress;
import com.atguigu.gmall.entity.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name,UserInfo userInfo);

    void delUser(UserInfo userInfo);

    UserInfo getUserById(String id);

    UserInfo login(UserInfo userInfo);

    UserInfo verfly(String userId);

    List<UserAddress> getAddress(String userId);
}

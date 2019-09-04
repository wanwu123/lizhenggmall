package com.lizhenggmall.user.service.impl;

import com.lizhenggmall.user.entity.User;
import com.lizhenggmall.user.mapper.UserMapper;
import com.lizhenggmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Override
    public User getUserById(String id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    @Autowired
    private UserMapper userMapper;
    @Override
    public List<User> getUserInfoListAll() {
        List<User> users = userMapper.selectAll();
        return users;
    }

    @Override
    public void addUser(User userInfo) {
        userMapper.insert(userInfo);
    }

    @Override
    public void updateUser(User userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, User userInfo) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("name",name);
        userMapper.updateByExample(userInfo,example);
    }

    @Override
    public void delUser(User userInfo) {
        userMapper.deleteByPrimaryKey(userInfo);
    }
}


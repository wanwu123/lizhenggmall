package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.UserService;

import com.atguigu.gmall.entity.UserInfo;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.lang.model.element.VariableElement;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Override
    public UserInfo verfly(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String user = jedis.get(userKey_prefix + userId + userinfoKey_suffix);
        UserInfo userInfo = JSON.parseObject(user, UserInfo.class);
        jedis.expire(userKey_prefix + userId + userinfoKey_suffix,userKey_timeOut);
        jedis.close();
        if (userInfo!=null){
            return userInfo;
        }
        return null;
    }

    @Autowired
    UserMapper userMapper;
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public UserInfo login(UserInfo userInfo) {
        //1查询
        String passwd = userInfo.getPasswd();
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(md5DigestAsHex);
        UserInfo userInfo1 = userMapper.selectOne(userInfo);
        if (userInfo1!=null){
            //加入缓存
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+userInfo1.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(userInfo1));
            jedis.close();
            return userInfo1;
        }else {
            return null;
        }


    }

    @Override
    public UserInfo getUserById(String id) {
        UserInfo user = userMapper.selectByPrimaryKey(id);
        return user;
    }


    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> users = userMapper.selectAll();
        return users;
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userMapper.insert(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
        userMapper.updateByExample(userInfo,example);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userMapper.deleteByPrimaryKey(userInfo);
    }
}


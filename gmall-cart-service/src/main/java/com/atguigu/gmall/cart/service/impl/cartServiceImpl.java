package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.CartService;
import com.atguigu.gamll.service.ManagerService;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.entity.CartInfo;
import com.atguigu.gmall.entity.SkuInfo;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class cartServiceImpl implements CartService{
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    
    @Reference
    private ManagerService managerService;
    @Override
    public CartInfo  addCart(String userId, String skuId, Integer num) {
        //加数据库
        //尝试取出已有数据 更新或者插入数据
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setSkuNum(num);
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfo);
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        if (cartInfoExists != null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        }else {
            CartInfo cartInfo2 = new CartInfo();
            cartInfo2.setSkuId(skuId);
            cartInfo2.setUserId(userId);
            cartInfo2.setSkuNum(num);
            cartInfo2.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo2.setSkuName(skuInfo.getSkuName());
            cartInfo2.setCartPrice(skuInfo.getPrice());
            cartInfo2.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo2);
            cartInfoExists = cartInfo2;
        }
        //加入缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:"+userId+":info";
        String jsonString = JSON.toJSONString(cartInfoExists);
        jedis.hset(cartKey,skuId,jsonString);
        jedis.close();

        //加数据库
        return cartInfoExists;
    }
}

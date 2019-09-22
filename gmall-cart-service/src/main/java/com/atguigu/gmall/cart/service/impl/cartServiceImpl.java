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
import sun.security.ec.SunEC;

import java.util.*;

@Service
public class cartServiceImpl implements CartService{
    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        String skuKey = "cart:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals(skuKey);
        List<CartInfo> cartList = new ArrayList<>();
        for (String hval : hvals) {
            CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
            cartList.add(cartInfo);
        }
        return cartList;
    }

    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        //用于检查缓存避免失效
        loadCartCacheIfNotExists(userId);
        String skuKey = "cart:"+userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(skuKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String toJSONString = JSON.toJSONString(cartInfo);
        jedis.hset(skuKey,skuId,toJSONString);
        String cartKey = "cart:"+userId+":checked";
        if (isChecked.equals("1")){
            jedis.hset(cartKey,skuId,toJSONString);
            jedis.expire(cartKey,60*60);
        }else{
            jedis.hdel(cartKey,skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {
        //先做合并
        cartInfoMapper.mergeCartInfo(userIdDest,userIdOrig);
        //删除临时车
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);
        //重新加载购物车
        List<CartInfo> cartInfos = loadCartCache(userIdDest);
        return cartInfos;
    }

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    
    @Reference
    private ManagerService managerService;

    @Override
    public List<CartInfo> cartList(String userId) {
        //查询缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:"+userId+":info";
        List<String> hvals = jedis.hvals(cartKey);
        List<CartInfo> cartList = new ArrayList<>();
        if (hvals!=null && hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>() {

                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            return cartList;
        }else {
            //缓存没有查数据库
            return loadCartCache(userId);
        }

    }
    public void  loadCartCacheIfNotExists(String userId){
        String cartkey="cart:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartkey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartkey,ttlInt+10);
        Boolean exists = jedis.exists(cartkey);
        jedis.close();
        if(!exists){
            loadCartCache( userId);
        }

    }
    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList =  cartInfoMapper.selectCartListWithSkuPrice(userId);
        //写入缓存
        if (cartInfoList!=null && cartInfoList.size()>0){
            Map<String , String> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
            }
            String cartKey = "cart:" + userId + ":info";
            Jedis jedis = redisUtil.getJedis();
            jedis.del(cartKey);
            jedis.hmset(cartKey,map);
            jedis.expire(cartKey,60*60*24);
            jedis.close();
        }
        return cartInfoList;
    }

    @Override
    public CartInfo  addCart(String userId, String skuId, Integer num) {
        loadCartCacheIfNotExists(userId);
        //加数据库
        //尝试取出已有数据 更新或者插入数据
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfo);
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        if (cartInfoExists != null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExists.setSkuPrice(skuInfo.getPrice());
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
        loadCartCache(userId);
        return cartInfoExists;
    }
}

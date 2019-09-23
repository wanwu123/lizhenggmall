package com.atguigu.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gmall.entity.OrderDetail;
import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService{
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));
        return orderInfo;
    }

    @Override
    public Boolean verifyToken(String userId,String token) {
        String tokenKey="user:"+userId+":trade_code";
        Jedis jedis = redisUtil.getJedis();
        String tokenExists = jedis.get(tokenKey);
        jedis.watch(tokenKey);
        Transaction transaction = jedis.multi();
        if (tokenExists!=null&&tokenExists.equals(token)){
            transaction.del(tokenKey);
        }
        List<Object> exec = transaction.exec();
        if (exec!=null&&exec.size()>0&&(Long)exec.get(0)==1L){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public String getToken(String userId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = "user:"+userId+":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tokenKey,10*60,token);
        jedis.close();
        return token;
    }
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return  orderInfo.getId();
    }
}

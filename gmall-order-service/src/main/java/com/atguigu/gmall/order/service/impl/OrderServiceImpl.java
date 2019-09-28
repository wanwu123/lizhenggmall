package com.atguigu.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gmall.entity.OrderDetail;
import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.enums.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService{

    @Reference
    private OrderService orderService;
    @Override
    public List<Map> orderSplit(String orderId, String wareSkuMap) {
        //先用orderId查询原始订单
        OrderInfo orderInfoParent = getOrderInfo(orderId);
        //wareSkuMap => list 遍历循环List
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        List<Map> wareParamMaoList = new ArrayList<>();
        //循环一次:生成一个订单 订单分成2个部分 主订单和订单明细
        for (Map wareSkumap : maps) {
            OrderInfo orderInfoSub = new OrderInfo();
            try {
                BeanUtils.copyProperties(orderInfoSub,orderInfoParent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            //子订单
            List<String> skuIds = (List<String>)wareSkumap.get("skuIds");//拆单方案
            List<OrderDetail> orderDetailList = orderInfoParent.getOrderDetailList();//现有父订单明细
            ArrayList<OrderDetail> orderDetailSubList = new ArrayList<>();//子订单明细
            for (String skuId : skuIds) {
                for (OrderDetail orderDetail : orderDetailList) {
                    if (skuId.equals(orderDetail.getSkuId())){
                        OrderDetail orderDetailSub = new OrderDetail();
                        orderDetailSub.setSkuId(orderDetail.getSkuId());
                        try {
                            BeanUtils.copyProperties(orderDetailSub,orderDetail);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        orderDetailSub.setId(null);
                        orderDetailSub.setOrderId(null);
                        orderDetailSubList.add(orderDetailSub);
                    }
                }
            }
            //组合完成一个子订单
            orderInfoSub.setOrderDetailList(orderDetailSubList);
            orderInfoSub.setId(null);
            orderInfoSub.setTotalAmount(orderInfoSub.getTotalAmount());
            orderInfoSub.setParentOrderId(orderInfoParent.getId());
            saveOrder(orderInfoSub);
            //把子订单包装成为库存模块 需要的结构 map
            String wareOrderString = orderService.initWareOrder(orderInfoSub.getId());
            Map map = JSON.parseObject(wareOrderString, Map.class);
            map.put("wareId", wareSkumap.get("wareId"));
            wareParamMaoList.add(map);
        }
        //组合成为List<Map>返回
        //原始订单状态修改为拆分
        updateOrederStatus(orderId,ProcessStatus.SPLIT);
        return wareParamMaoList;
    }

    @Override
    @Async
    public void handleExpiredCoupon(Integer id) {
        try {
            System.out.println("购物券:"+id+"发送");
            Thread.sleep(1000);
            System.out.println("购物券:"+id+"删除1");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Integer> checkExpiredCoupon() {
        return Arrays.asList(1,2,3,4,5,6,7);
    }

    @Override
    public void updateOrederStatus(String orderId, ProcessStatus paid,OrderInfo... orderInfos) {
        OrderInfo orderInfo = new OrderInfo();
        if (orderInfos!=null && orderInfos.length>0){//如果还需更新其他订单信息则使用可变参数
            orderInfo  = orderInfos[0];
        }
        orderInfo.setProcessStatus(paid);
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

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

    /**
     * 初始化发送到库存系统的参数
     * @param orderId
     * @return
     */

    public String initWareOrder(String orderId){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        if (orderInfo != null){
            Map map = new HashMap();
            map.put("orderId",orderId);
            map.put("consignee",orderInfo.getConsignee());
            map.put("consigneeTel",orderInfo.getConsigneeTel());
            map.put("orderComment",orderInfo.getOrderComment());
            map.put("orderBody",orderInfo.genSubject());
            map.put("deliveryAddress",orderInfo.getDeliveryAddress());
            map.put("paymentWay","2");
            map.put("wareId",orderInfo.getWareId());
            List detailList = new ArrayList();
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
            for (OrderDetail orderDetail : orderDetailList) {
                Map<Object, Object> demap = new HashMap<>();
                demap.put("skuId",orderDetail.getSkuId());
                demap.put("skuName",orderDetail.getSkuName());
                demap.put("skuNum",orderDetail.getSkuNum());
                detailList.add(demap);
            }
            map.put("details",detailList);
            return JSON.toJSONString(map);
        }
        return null;
    }
}

package com.atguigu.gmall.order.consumer;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.service.OrderService;
import com.atguigu.gmall.entity.OrderDetail;
import com.atguigu.gmall.entity.OrderInfo;
import com.atguigu.gmall.entity.enums.ProcessStatus;
import com.atguigu.gmall.util.ActiveMqUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class OrderConsumer  {

    @Reference
    private OrderService orderService;
    @Autowired
    private ActiveMqUtil activeMqUtil;

    @JmsListener(destination = "SKU_DELIVER_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerWareDeliver(MapMessage mapMessage) throws JMSException {
        String orderId= mapMessage.getString("orderId");
        String status= mapMessage.getString("status");
        String trackingNo= mapMessage.getString("trackingNo");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTrackingNo(trackingNo);
        if ("DEDUCTED".equals(status)){
            orderService.updateOrederStatus(orderId, ProcessStatus.DELEVERED,orderInfo);
        }

    }
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener"/*,concurrency = "3"*/)
    public void consumerWareDeduct(MapMessage mapMessage) throws JMSException {
        String orderId= mapMessage.getString("orderId");
        String status= mapMessage.getString("status");
        if ("DEDUCTED".equals(status)){
            orderService.updateOrederStatus(orderId, ProcessStatus.WAITING_DELEVER);
        }else {
            orderService.updateOrederStatus(orderId , ProcessStatus.STOCK_EXCEPTION);
        }
    }
    @JmsListener(destination = "duilie",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        if ("success".equals(result)){
            System.out.println("订单OK"+orderId+result);
            //修改订单状态
            orderService.updateOrederStatus(orderId, ProcessStatus.PAID);
            sendOrderToWare(orderId);
        }

    }

    public void sendOrderToWare(String orderId) throws JMSException {
        String initWareOrderString = orderService.initWareOrder(orderId);
        Connection connection = activeMqUtil.getConnection();
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue sendOrder = session.createQueue("ORDER_RESULT_QUEUE");
        MessageProducer producer = session.createProducer(sendOrder);
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText(initWareOrderString);
        producer.send(activeMQTextMessage);
        session.commit();
        session.close();
        producer.close();
        connection.close();
        orderService.updateOrederStatus(orderId, ProcessStatus.NOTIFIED_WARE);
    }


}

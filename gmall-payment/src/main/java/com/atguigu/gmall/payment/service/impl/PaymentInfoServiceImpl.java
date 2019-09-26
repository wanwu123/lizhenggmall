package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayAcquireQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.entity.enums.PaymentStatus;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gamll.service.PaymentService;
import com.atguigu.gmall.entity.PaymentInfo;
import com.atguigu.gmall.util.ActiveMqUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentInfoServiceImpl implements PaymentService{

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private ActiveMqUtil activeMqUtil;


    @Reference
    private PaymentService paymentService;
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerDelayPaymentResult(MapMessage mapMessage) throws JMSException {
        Long delaySec = mapMessage.getLong("delaySec");
        String outTradeNo = mapMessage.getString("outTradeNo");
        Integer checkCount = mapMessage.getInt("checkCount");
        //判断是否需要检查
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);
        if (paymentInfo.getPaymentStatus()!=PaymentStatus.UNPAID){
            return;
        }
        //如果未付款查支付宝
        PaymentStatus paymentStatus = checkAlipayment(paymentInfoQuery);
        if (paymentStatus==PaymentStatus.PAID){
            //修改状态
            paymentInfoQuery.setPaymentStatus(PaymentStatus.PAID);
            updatePaymentInfo(outTradeNo,paymentInfoQuery);
            sendPaymentToOrder(paymentInfo.getOrderId(),"success");
        }else if (paymentStatus == PaymentStatus.UNPAID){
            //如果未付款
            if (checkCount>0){
                checkCount--;
                sendDelayPaymentResult(outTradeNo,delaySec,checkCount);
            }
        }
    }

    public void sendDelayPaymentResult(String outTradeNo,Long delaySec,Integer checkCount) {
        Connection connection = activeMqUtil.getConnection();
        Session session = null;
        try {
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo",outTradeNo);
            activeMQMapMessage.setLong("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            producer.send(activeMQMapMessage);
            session.commit();
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
    /**
     * 查询支付状态
     * @param paymentInfo
     * @return
     */
    public PaymentStatus checkAlipayment(PaymentInfo paymentInfo){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no",paymentInfo.getOutTradeNo());
        request.setBizContent(jsonObject.toJSONString());
        AlipayTradeQueryResponse response = null;
        try {
            response  = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()){
            System.out.println("调用成功");
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())){
                return PaymentStatus.PAID;
            }else if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())){
                return PaymentStatus.UNPAID;
            }
        }else {
            System.out.println("调用失败");
            return PaymentStatus.UNPAID;
        }
        return null;
    }

    @Override
    public void sendPaymentToOrder(String orderId,String result) {
        Connection connection = activeMqUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue duilie = session.createQueue("duilie");
            MessageProducer producer = session.createProducer(duilie);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",orderId);
            mapMessage.setString("result",result);
            producer.send(mapMessage);
            session.commit();
            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentMapper.updateByExampleSelective(paymentInfo,example);

    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        PaymentInfo paymentInfo1 = paymentMapper.selectOne(paymentInfo);
        return paymentInfo1;
    }

    @Autowired
    private PaymentMapper paymentMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }
}

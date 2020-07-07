package com.hmall.order.service.mq;

import com.hmall.pojo.ProcessStatus;
import com.hmall.service.OrderService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

/**
 *订单消息消费
 */
@Component
public class OrderListener {

    /**
     * 注入订单业务逻辑
     */
    @Reference()
    private OrderService orderService;

    /**
     * 消费支付后生产的信息
     * @param message
     * @throws JMSException
     */
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(Message message) throws JMSException {
        if(message instanceof  MapMessage){
            MapMessage mapMessage= (MapMessage) message;
            String result = mapMessage.getString("result");
            String orderId = mapMessage.getString("orderId");
            if("success".equals(result)){
                //更新订单状态为支付
                orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                //发送消息给仓库
                orderService.notifyWare(orderId);
                //更新状态为通知仓库
                orderService.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
            }else {
                //更新状态为未支付
                orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
            }
        }
    }


    /**
     * 消费仓库库存生产的信息
     * @param message
     * @throws JMSException
     */
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(Message message) throws Exception {
        if(message instanceof  MapMessage){
            MapMessage mapMessage= (MapMessage) message;
            String status = mapMessage.getString("status");
            String orderId = mapMessage.getString("orderId");
            if("DEDUCTED".equals(status)) {
                //减库存，修改订单状态
                orderService.updateOrderStatus(orderId,ProcessStatus.WAITING_DELEVER);
                //
                message.acknowledge();
            }
            if("OUT_OF_STOCK".equals(status)) {
                orderService.updateOrderStatus(orderId,ProcessStatus.STOCK_EXCEPTION);
                //TODO 超卖，记录日志，通知客服加库存，抛异常回滚消息到mq中重新消费
                throw  new Exception("当前订单库存出现异常");
            }
        }
    }
}

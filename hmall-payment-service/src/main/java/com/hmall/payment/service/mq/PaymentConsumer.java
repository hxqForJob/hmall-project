package com.hmall.payment.service.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.PaymentInfo;
import com.hmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * 支付消息消费
 */
@Component
public class PaymentConsumer {

    //注入支付业务逻辑
    @Reference
    private PaymentService paymentService;

    //延迟队列，消费支付信息，确保支付完成
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {

        // 获取消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        // 创建一个paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo.getOutTradeNo());
        // 调用 paymentService.checkPayment(paymentInfoQuery);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        System.out.println("检查结果："+flag);
        //如果没有支付完成且检查次数没有达到预设值，重新消费
        if (!flag && checkCount!=0){
            // 还需要继续检查
            System.out.println("检查的次数："+checkCount);
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }
    }
}


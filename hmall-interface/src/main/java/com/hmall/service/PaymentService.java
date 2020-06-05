package com.hmall.service;

import com.hmall.pojo.PaymentInfo;

import java.util.Map;

/**
 * 支付业务逻辑
 */
public interface PaymentService {

    /**
     * 添加支付信息
     * @param paymentInfo
     */
   void addOrUpdatePaymentInfo(PaymentInfo paymentInfo);

    /**
     *根据交易编号获取获取支付信息
     * @param out_trade_no
     * @return
     */
    PaymentInfo getPaymentInfo(String out_trade_no);

    /**
     * 更新支付信息
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 根据订单Id查询支付信息
     * @param orderId
     * @return
     */
    PaymentInfo getPaymentInfoByOrderId(String orderId);

    /**
     * 发送消息，通知订单
     * @param orderId
     * @param result
     */
    void notifyOrder(String orderId,String result);

 /**
  * 微信支付
  * @param orderId 订单Id
  * @param totalFee 订单价格分
  * @return
  */
 Map createNative(String orderId, String totalFee);


 /**
  * 查询支付宝该订单是否支付
  * @param paymentInfoQuery
  * @return
  */
 boolean checkPayment(PaymentInfo paymentInfoQuery);

 /**
  * 发送延迟队列，判断是否支付完成
  * @param outTradeNo 支付宝交易编号
  * @param delaySec 延迟秒数
  * @param checkCount 重发次数
  */
 void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

 /**
  * 关闭付款信息
  * @param id
  */
 void closePayment(String id);
}

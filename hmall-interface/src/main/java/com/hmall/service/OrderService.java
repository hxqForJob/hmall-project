package com.hmall.service;

import com.hmall.pojo.OrderInfo;
import com.hmall.pojo.ProcessStatus;

import java.util.List;
import java.util.Map;

/**
 * 订单业务逻辑接口
 */
public interface OrderService {
    /**
     * 初始化交易编号
     * @param userId
     * @return
     */
    String initTradeNo(String userId);

    /**
     * 删除交易编号
     * @param userId
     */
    void delTradeNo(String userId);


    /**
     * 获取交易编号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 添加订单并返回订单Id
     * @param orderInfo
     * @return
     */
    String  addOrderInfo(OrderInfo orderInfo);


    /**
     * 获取订单和订单详细
     * @param orderId
     * @return
     */
    OrderInfo getOrderAndDetail(String  orderId);

    /**
     * 获取订单
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 检验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);


    /**
     * 更新订单状态
     * @param orderId
     * @param paid
     */
    void updateOrderStatus(String orderId, ProcessStatus paid);

    /**
     * 发送消息通知仓库
     * @param orderId
     */
    void notifyWare(String orderId);

    /**
     * 获取过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 关闭过期订单，和关闭过期支付信息
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    /**
     * 根据不同仓库拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<Map> splitOrder(String orderId, List<Map> wareSkuMap);
}

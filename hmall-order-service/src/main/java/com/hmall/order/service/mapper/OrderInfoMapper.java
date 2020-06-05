package com.hmall.order.service.mapper;

import com.hmall.pojo.OrderInfo;
import tk.mybatis.mapper.common.Mapper;

public interface OrderInfoMapper extends Mapper<OrderInfo> {
    /**
     * 根据id查询订单和订单明细
     * @param orderId
     * @return
     */
    OrderInfo getOrderAndDetail(Integer orderId);
}

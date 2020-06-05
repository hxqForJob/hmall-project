package com.hmall.order.service.task;

import com.hmall.pojo.OrderInfo;
import com.hmall.service.OrderService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单轮询任务
 */
@EnableScheduling //开启轮询任务
@Component
public class OrderTask {

    /**
     * 订单业务逻辑
     */
    @Reference
    private OrderService orderService;

    /**
     * 关闭未支付的订单，每隔20秒扫描一次
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public  void checkOrder(){
        System.out.println("开始处理过期订单");
        long starttime = System.currentTimeMillis();
        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();
        for (OrderInfo orderInfo : expiredOrderList) {
            // 处理未完成订单
            orderService.execExpiredOrder(orderInfo);
        }
        long costtime = System.currentTimeMillis() - starttime;
        System.out.println("一共处理"+expiredOrderList.size()+"个订单 共消耗"+costtime+"毫秒");
    }

}

package com.hmall.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单信息
 */
@Data
public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; //主键

    @Column
    private String consignee; //收货人

    @Column
    private String consigneeTel;//收货人电话


    @Column
    private BigDecimal totalAmount; //总金额

    @Column
    private OrderStatus orderStatus;//订单状态

    @Column
    private ProcessStatus processStatus;//订单进度状态


    @Column
    private String userId; //用户Id

    @Column
    private PaymentWay paymentWay;//支付方式

    @Column
    private Date expireTime;//过期时间

    @Column
    private String deliveryAddress; //收货地址

    @Column
    private String orderComment;//订单状态

    @Column
    private Date createTime;//创建时间

    @Column
    private String parentOrderId;//拆单父Id，默认为空

    @Column
    private String trackingNo;//物流编号


    @Transient
    private List<OrderDetail> orderDetailList; //订单明细


    @Transient
    private String wareId;

    @Column
    private String outTradeNo;//第三方支付编号

    public void sumTotalAmount(){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount= totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount=  totalAmount;
    }

    /**
     * 获取订单大致信息
     * @return
     */
    public String getTradeBody(){
        OrderDetail orderDetail = orderDetailList.get(0);
        if(orderDetail!=null){
            String tradeBody=orderDetail.getSkuName()+"等"+orderDetailList.size()+"件商品";
            return tradeBody;
        }
      return  "没有商品";
    }


}

package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付信息
 */
@Data
public class PaymentInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String  id; //主键

    /**
     *  订单中已生成的对外交易编号。订单中获取
     */
    @Column
    private String outTradeNo;

    /**
     * 订单Id，外键
     */
    @Column
    private String orderId;

    /**
     * 订单编号  初始为空，支付宝回调时生成
     */
    @Column
    private String alipayTradeNo;

    /**
     * 总金额
     */
    @Column
    private BigDecimal totalAmount;

    /**
     * 描述
     */
    @Column
    private String Subject;

    /**
     * 当前支付状态
     */
    @Column
    private PaymentStatus paymentStatus;

    /**
     * 创建时间
     */
    @Column
    private Date createTime;

    /**
     * 回调时间
     */
    @Column
    private Date callbackTime;

    /**
     * 回调内容
     */
    @Column
    private String callbackContent;

}

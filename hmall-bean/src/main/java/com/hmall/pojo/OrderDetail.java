package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
public class OrderDetail implements Serializable {
    @Id
    @Column
    private String id; //明细Id
    @Column
    private String orderId;  //订单Id
    @Column
    private String skuId;//商品Id
    @Column
    private String skuName;//商品名称
    @Column
    private String imgUrl;//图片地址
    @Column
    private BigDecimal orderPrice;//价格
    @Column
    private Integer skuNum;//数量

    @Transient
    private String hasStock;//是否有库存
}


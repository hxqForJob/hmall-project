package com.hmall.pojo;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车实体
 */
@Data
public class CartInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id; //购物车Id
    @Column
    String userId; //用户Id
    @Column
    String skuId; //商品Id
    @Column
    BigDecimal cartPrice; //购物车中价格
    @Column
    Integer skuNum; //数量
    @Column
    String imgUrl; //默认图片
    @Column
    String skuName; //sku名称
    // 实时价格
    @Transient
    BigDecimal skuPrice;
    // 下订单的时候，商品是否勾选
    @Transient
    String isChecked="0";
}

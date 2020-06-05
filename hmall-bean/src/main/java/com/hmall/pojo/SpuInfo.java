package com.hmall.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Spu商品信息
 */
@Data
public class SpuInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; //主键

    @Column
    private String spuName; //商品名

    @Column
    private String description; //描述

    @Column
    private  String catalog3Id; //三级分类Id

    @Transient
    private List<SpuSaleAttr> spuSaleAttrList; //销售属性集合
    @Transient
    private List<SpuImage> spuImageList;//商品图片集合


}


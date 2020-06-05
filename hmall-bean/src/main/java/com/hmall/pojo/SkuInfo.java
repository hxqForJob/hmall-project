package com.hmall.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

//sku基本信息
@Data
public class SkuInfo implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  String id;//主键
    @Column
    private String spuId;//spuId
    @Column
    private Double price;//价格
    @Column
    private  String skuName;//sku名称
    @Column
    private  String skuDesc;//sku描述
    @Column
    private  Double weight;//重量
    @Column
    private String tmId;//品牌
    @Column
    private  String catalog3Id;//三级分类
    @Column
    private String skuDefaultImg; //默认图片

    @Transient
    private List<SkuImage> skuImageList; //图片集合

    @Transient
    private List<SkuAttrValue> skuAttrValueList;//平台属性值集合

    @Transient
    private  List<SkuSaleAttrValue> skuSaleAttrValueList;//销售属性值集合
}

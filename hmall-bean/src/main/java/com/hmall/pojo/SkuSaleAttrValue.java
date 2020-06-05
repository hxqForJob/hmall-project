package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class SkuSaleAttrValue implements Serializable {

    @Column
    @Id
    private String id;//主键
    @Column
    private  String skuId;//skuId
    @Column
    private  String saleAttrId;//销售属性Id
    @Column
    private  String saleAttrValueId;//销售属性值Id
    @Column
    private  String saleAttrName;//销售属性名称
    @Column
    private  String saleAttrValueName;//销售属性值名称
}

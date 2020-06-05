package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

//Sku平台属性值
@Data
public class SkuAttrValue implements Serializable {

    @Column
    @Id
    private String id; //主键
    @Column
    private  String attrId; //平台属性Id
    @Column
    private String valueId; //平台属性值
    @Column
    private  String skuId;//skuId
}

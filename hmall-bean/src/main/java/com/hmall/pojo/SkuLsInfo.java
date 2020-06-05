package com.hmall.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 添加到es的sku信息
 */
@Data
public class SkuLsInfo implements Serializable {

    private String id; //id SkuId

    private Double price; //价格

    private String skuName; //Sku名称

    private String catalog3Id; //三级分类

    private  String skuDefaultImg; //sku默认图片

    private Long hotScore=0L; //热度

    private List<SkuLsAttrValue> skuAttrValueList; //平台属性值Id集合
}


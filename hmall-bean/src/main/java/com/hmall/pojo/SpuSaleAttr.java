package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

//商品销售属性
@Data
public class SpuSaleAttr implements Serializable {

    @Id
    @Column
    String id ; //id

    @Column
    String spuId; //商品Id

    @Column
    String saleAttrId; //销售属性Id

    @Column
    String saleAttrName; //销售属性名


    @Transient
    List<SpuSaleAttrValue> spuSaleAttrValueList; //销售属性值

}

package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 销售属性值
 */
@Data
public class SpuSaleAttrValue implements Serializable {
    @Id
    @Column
    String id ; //id

    @Column
    String spuId; //商品id

    @Column
    String saleAttrId; //销售属性Id

    @Column
    String saleAttrValueName; //销售属性名

    @Transient
    String isChecked; //是否选中
}

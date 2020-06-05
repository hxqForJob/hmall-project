package com.hmall.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * 平台属性
 */
@Data
public class BaseAttrInfo implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private String id; //id
    @Column
    private String attrName; //属性名
    @Column
    private String catalog3Id; //三级分类

    @Transient
    private List<BaseAttrValue>  attrValueList; //属性下的属性值

}

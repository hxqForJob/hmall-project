package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * 基础销售属性
 */
@Data
public class BaseSaleAttr implements Serializable {
    @Id
    @Column
    String id ;//主键

    @Column
    String name;//属性名称

}


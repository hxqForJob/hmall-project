package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 二级分类实体
 */
@Data
public class BaseCatalog2 implements Serializable {
    @Id
    @Column
    private String id; //id
    @Column
    private String name; //分类名称
    @Column
    private String catalog1Id;//一级分类Id


}

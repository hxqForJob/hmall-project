package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 三级分类实体
 */
@Data
public class BaseCatalog3 implements Serializable {
    @Id
    @Column
    private String id; //id
    @Column
    private String name; //分类名称
    @Column
    private String catalog2Id;//二级分类Id


}

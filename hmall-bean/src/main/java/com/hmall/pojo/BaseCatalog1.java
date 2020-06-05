package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 分类一实体
 */
@Data
public class BaseCatalog1 implements Serializable {
    @Id
    @Column
    private String id; //id
    @Column
    private String name; //分类名称

}

package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 平台属性值
 */
@Data
public class BaseAttrValue implements Serializable {
    @Id
    @Column
    private String id; //主键
    @Column
    private String valueName; //属性值
    @Column
    private String attrId; //属性Id

}

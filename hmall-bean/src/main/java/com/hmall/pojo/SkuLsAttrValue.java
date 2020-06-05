package com.hmall.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传到es中Sku中平台属性Id
 */
@Data
public class SkuLsAttrValue implements Serializable {

    private String valueId; //平台属性值Id;
}

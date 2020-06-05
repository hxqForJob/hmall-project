package com.hmall.pojo;

import lombok.Data;

import java.io.Serializable;


/**
 * 查询Sku参数
 */
@Data
public class SkuLsParams implements Serializable {

    /**
     * 搜索框关键词
     */
    private String  keyword; //

    /**
     * 三级分类Id
     */
    private String catalog3Id;

    /**
     * 平台属性值Id集合
     */
    private String[] valueId;

    /**
     * 页码 默认1
     */
    private int pageNo=1;

    /**
     * 页大小 默认20
     */
    private int pageSize=20;

    private String orderBy="hotScore";

    private Integer owp=1; //价格排序方式，1升序，0降序

}

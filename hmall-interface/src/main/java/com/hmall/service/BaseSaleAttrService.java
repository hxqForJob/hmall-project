package com.hmall.service;

import com.hmall.pojo.BaseSaleAttr;
import com.hmall.pojo.SpuSaleAttr;

import java.util.List;

/**
 * 后台管理销售属性业务逻辑接口
 */
public interface BaseSaleAttrService {

    /**
     * 获取所有销售属性
     * @return
     */
     List<BaseSaleAttr> findAll();





}

package com.hmall.service;

import com.hmall.pojo.SpuSaleAttr;

import java.util.List;

//后台管理Spu销售属性业务逻辑
public interface SpuSaleAtrrService {
    /**
     * 根据spuId查询spu销售属性和属性值
     */
    List<SpuSaleAttr> getSpuSaleAttrInfoBySpuId(Integer spuId);

    /**
     * 根据spudid和skuid 获取销售属性值和选中当前sku销售属性
     * @param spuId
     * @param id
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrAndSelectValue(String spuId, String id);
}

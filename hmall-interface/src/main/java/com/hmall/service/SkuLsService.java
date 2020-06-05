package com.hmall.service;

import com.hmall.pojo.SkuLsParams;
import com.hmall.pojo.SkuLsResult;

/**
 * 商品列表业务逻辑接口
 */
public interface SkuLsService {

    /**
     * 查询sku
     * @param skuLsParams
     * @return
     */
    SkuLsResult getSkuLsResult(SkuLsParams skuLsParams);

    /**
     * 添加商品访问量
     * @param skuId
     */
    void addWatch(Integer skuId);
}

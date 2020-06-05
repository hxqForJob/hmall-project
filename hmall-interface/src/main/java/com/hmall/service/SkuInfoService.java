package com.hmall.service;

import com.hmall.pojo.SkuInfo;
import com.hmall.pojo.SkuSaleAttrValue;

import java.util.List;

//后台管理Sku业务逻辑接口
public interface SkuInfoService {

    /**
     * 添加sku
     * @param skuInfo
     * @return
     */
    boolean saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据Id查询SkuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoById(Integer skuId);

    /**
     * 根据Id查询SkuInfo和图片
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoAndImageById(Integer skuId);

    /**
     * 获取当前spu下sku的销售属性值
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getAllSkuSaleAttrValueCom(String spuId);

    /**
     * 上架Sku
     * @param skuId
     */
    void onSale(String skuId);
}

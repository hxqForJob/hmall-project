package com.hmall.service;

import com.hmall.pojo.SpuImage;
import com.hmall.pojo.SpuInfo;

import java.util.List;

/**
 * 后台管理商品信息业务逻辑接口
 */
public interface SpuInfoService {

    //根据三级分类获取商品信息
    List<SpuInfo> getSpuInfoByCatalogId3(String cataLogId3);

    //添加商品信息
    void  addSpuInfo(SpuInfo spuInfo);

    //获取商品图片
    List<SpuImage> spuImageList(String spuId);
}

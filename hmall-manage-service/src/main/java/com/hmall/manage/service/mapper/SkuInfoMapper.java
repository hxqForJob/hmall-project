package com.hmall.manage.service.mapper;

import com.hmall.pojo.SkuInfo;
import tk.mybatis.mapper.common.Mapper;

public interface SkuInfoMapper extends Mapper<SkuInfo> {
    /**
     * 根据Id获取Sku和Sku图片
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoAndImageById(Integer skuId);
}

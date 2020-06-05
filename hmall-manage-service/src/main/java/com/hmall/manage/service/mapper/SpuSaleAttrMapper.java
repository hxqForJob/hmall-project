package com.hmall.manage.service.mapper;

import com.hmall.pojo.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//商品销售属性Mapper
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    //根据SpuId查询spu销售属性
    List<SpuSaleAttr> getSpuSaleAttrInfoBySpuId(Integer spuId);

    //根据spuId和skuiId查询spu销售属性值和选中当前sku销售属性值
    List<SpuSaleAttr> getSpuSaleAttrAndSelectValue(Integer valueOf, Integer valueOf1);
}

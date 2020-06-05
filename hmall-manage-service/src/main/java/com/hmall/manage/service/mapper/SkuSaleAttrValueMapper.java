package com.hmall.manage.service.mapper;

import com.hmall.pojo.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    /**
     * 根据spuId查询所有sku销售属性值
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getBySpuId(Integer spuId);
}

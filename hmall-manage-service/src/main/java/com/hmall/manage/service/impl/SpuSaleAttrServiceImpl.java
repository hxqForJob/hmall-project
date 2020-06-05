package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.manage.service.mapper.SpuSaleAttrMapper;
import com.hmall.pojo.SpuSaleAttr;
import com.hmall.service.SpuSaleAtrrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//商品销售属性
@Service
public class SpuSaleAttrServiceImpl implements SpuSaleAtrrService {

    //注入商品销售属性数据访问
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    /**
     * 根据SpuId查询spu销售属性和属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrInfoBySpuId(Integer spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrInfoBySpuId(spuId);
    }

    /**
     * 根据spudid和skuid 获取销售属性值和选中当前sku销售属性
     * @param spuId
     * @param id
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrAndSelectValue(String spuId, String id) {
        return spuSaleAttrMapper.getSpuSaleAttrAndSelectValue(Integer.valueOf(spuId),Integer.valueOf(id));
    }
}

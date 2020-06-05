package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.manage.service.mapper.BaseSaleAttrMapper;
import com.hmall.pojo.BaseSaleAttr;
import com.hmall.pojo.SpuSaleAttr;
import com.hmall.service.BaseSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 销售属性业务逻辑属性
 */
@Service
public class BaseSaleAttrServiceImpl implements BaseSaleAttrService {

    //销售属性数据访问
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    /**
     * 获取所有销售属性
     * @return
     */
    @Override
    public List<BaseSaleAttr> findAll() {
        return baseSaleAttrMapper.selectAll();
    }


}

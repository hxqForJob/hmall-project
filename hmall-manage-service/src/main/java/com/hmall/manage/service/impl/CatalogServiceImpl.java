package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.manage.service.mapper.Catalog1Mapper;
import com.hmall.manage.service.mapper.Catalog2Mapper;
import com.hmall.manage.service.mapper.Catalog3Mapper;
import com.hmall.pojo.BaseCatalog1;
import com.hmall.pojo.BaseCatalog2;
import com.hmall.pojo.BaseCatalog3;
import com.hmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 分类业务逻辑实现
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    //一级分类数据访问
    @Autowired
    private Catalog1Mapper catalog1Mapper;
    //二级分类数据访问
    @Autowired
    private Catalog2Mapper catalog2Mapper;
    //三级分类数据访问
    @Autowired
    private Catalog3Mapper catalog3Mapper;

    /**
     * 查询一级分类
     * @return
     */
    @Override
    public List<BaseCatalog1> getBaseCatalog1s() {
        return catalog1Mapper.selectAll();
    }

    /**
     * 获取二级分类
     * @param catalog1Id
     * @return
     */
    @Override
    public List<BaseCatalog2> getBaseCatalog2s(String catalog1Id) {
        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return catalog2Mapper.select(baseCatalog2);
    }

    /**
     * 获取三级分类
     * @param catalog2Id
     * @return
     */
    @Override
    public List<BaseCatalog3> getBaseCatalog3s(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return catalog3Mapper.select(baseCatalog3);
    }
}

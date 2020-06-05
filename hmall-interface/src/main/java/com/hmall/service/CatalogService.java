package com.hmall.service;

import com.hmall.pojo.BaseCatalog1;
import com.hmall.pojo.BaseCatalog2;
import com.hmall.pojo.BaseCatalog3;

import java.util.List;

/**
 * 后台管理分类业务逻辑接口
 */
public interface CatalogService {

    /**
     *  获取一级分类
     * @return
     */
    List<BaseCatalog1> getBaseCatalog1s();

    /**
     * 根据一级分类Id获取二级分类
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getBaseCatalog2s(String catalog1Id);

    /**
     * 根据二级分类Id获取三级分类
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getBaseCatalog3s(String catalog2Id);

}

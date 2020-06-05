package com.hmall.service;

import com.hmall.pojo.BaseAttrInfo;
import com.hmall.pojo.BaseSaleAttr;

import java.util.List;

/**
 * 后台管理平台属性业务逻辑接口
 */
public interface BaseAttrService {

    /**
     * 根据三级分类Id获取平台属性
     * @param catalog3Id
     * @return
     */
     List<BaseAttrInfo> getBaseAttrByCataId(String catalog3Id);

    /**
     * 根据Id查询平台属性
     * @param baseAttrId
     * @return
     */
     BaseAttrInfo getBaseAttrById(String baseAttrId);

    /**
     * 更新或添加平台属性
     * @param baseAttrInfo
     */
    void  saveOrUpdate(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性值Id获取平台属性
     * @return
     */
    List<BaseAttrInfo> findByValueId(String  valueIds);


}

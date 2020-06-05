package com.hmall.manage.service.mapper;

import com.hmall.pojo.BaseAttrInfo;
import com.hmall.pojo.BaseCatalog2;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 平台属性数据访问
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    /**
     * 根据三级分类id查询平台属性和平台属性值
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrByCataId(Integer catalog3Id);

    List<BaseAttrInfo> findByValueId(@Param("valueIds") String valueIds);
}

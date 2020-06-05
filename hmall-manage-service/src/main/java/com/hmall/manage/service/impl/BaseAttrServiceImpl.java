package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.manage.service.mapper.BaseAttrInfoMapper;
import com.hmall.manage.service.mapper.BaseAttrValMapper;
import com.hmall.pojo.BaseAttrInfo;
import com.hmall.pojo.BaseAttrValue;
import com.hmall.service.BaseAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * 平台属性业务逻辑
 */
@Service
public class BaseAttrServiceImpl implements BaseAttrService {

    //注入平台属性数据访问
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    //注入平台属性值数据访问
    @Autowired
    private BaseAttrValMapper baseAttrValMapper;

    /**
     * 根据三级分类Id查询平台属性
     * @param catalog3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrByCataId(String catalog3Id) {

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.getBaseAttrByCataId(Integer.valueOf(catalog3Id));
        return  baseAttrInfos;
    }

    /**
     * 根据Id查询平台属性
     * @param baseAttrId
     * @return
     */
    @Override
    public BaseAttrInfo getBaseAttrById(String baseAttrId) {
        BaseAttrInfo baseAttrInfo=baseAttrInfoMapper.selectByPrimaryKey(baseAttrId);
        //查询属性值
        BaseAttrValue baseAttrValue=new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(baseAttrValues);
        return  baseAttrInfo;
    }

    /**
     * 保存或添加平台属性
     * @param baseAttrInfo
     */
    @Override
    @Transactional
    public void saveOrUpdate(BaseAttrInfo baseAttrInfo) {
        String baseInfoId=baseAttrInfo.getId();
        //判断是添加还是修改
    if(StringUtils.isEmpty(baseInfoId)){
        //添加平台属性
        baseAttrInfoMapper.insert(baseAttrInfo);
    }else {
        //修改平台属性
        baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        //删除原来的平台属性值
        BaseAttrValue attrValue=new BaseAttrValue();
        attrValue.setAttrId(baseInfoId);
        baseAttrValMapper.delete(attrValue);
    }
    //添加平台属性值
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue atrriValue : baseAttrInfo.getAttrValueList()) {
                //添加平台属性值
                atrriValue.setAttrId(baseAttrInfo.getId());
                baseAttrValMapper.insert(atrriValue);
            }
        }
    }

    /**
     * 根据平台属性值Id查询平台属性
     * @param valueIds
     * @return
     */
    @Override
    public List<BaseAttrInfo> findByValueId(String valueIds) {
        return baseAttrInfoMapper.findByValueId(valueIds);
    }
}

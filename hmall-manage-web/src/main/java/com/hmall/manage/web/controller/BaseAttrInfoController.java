package com.hmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.BaseAttrInfo;
import com.hmall.pojo.BaseAttrValue;
import com.hmall.service.BaseAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台属性控制器
 */
@RestController
@CrossOrigin
public class BaseAttrInfoController {

    /**
     * 注入平台属性业务逻辑服务
     */
    @Reference
    private BaseAttrService baseAttrService;

    /**
     * 根据三级分类查询平台属性
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        List<BaseAttrInfo> baseAttrInfos = baseAttrService.getBaseAttrByCataId(catalog3Id);
        return  baseAttrInfos;
    }

    /**
     * 添加或修改平台属性
     * @param baseAttrInfo
     */
    @RequestMapping("/saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
    baseAttrService.saveOrUpdate(baseAttrInfo);
    }

    /**
     * 根据id查询属性
     * @param attrId
     * @return
     */
    @RequestMapping("/getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo baseAttrInfo = baseAttrService.getBaseAttrById(attrId);
        return  baseAttrInfo.getAttrValueList();
    }
}

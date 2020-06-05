package com.hmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.BaseCatalog1;
import com.hmall.pojo.BaseCatalog2;
import com.hmall.pojo.BaseCatalog3;
import com.hmall.service.CatalogService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@CrossOrigin
public class CatalogController {

    /**
     * 分类业务逻辑
     */
    @Reference
    private CatalogService catalogService;

    /**
     * 获取一级分类
     * @return
     */
    @RequestMapping("/getCatalog1")
    public List<BaseCatalog1> getCatalog1()
    {
        List<BaseCatalog1> catalog1s = catalogService.getBaseCatalog1s();
        return  catalog1s;
    }


    //http://localhost:8082/getCatalog2?catalog1Id=2
    /**
     * 获取二级分类
     * @return
     */
    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id)
    {
        List<BaseCatalog2> catalog2s = catalogService.getBaseCatalog2s(catalog1Id);
        return  catalog2s;
    }

    /**
     * 获取三级分类
     * @return
     */
    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id)
    {
        List<BaseCatalog3> catalog3s = catalogService.getBaseCatalog3s(catalog2Id);
        return  catalog3s;
    }
}

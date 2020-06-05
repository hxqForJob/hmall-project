package com.hmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.SkuInfo;
import com.hmall.service.SkuInfoService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    private SkuInfoService skuInfoService;

    @RequestMapping("/saveSkuInfo")
    public  String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        boolean result = skuInfoService.saveSkuInfo(skuInfo);
        return  result?"添加成功！":"添加失败！";
    }

    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @RequestMapping("/onSale")
    public String onSale(String skuId){
        skuInfoService.onSale(skuId);
        return "true";
    }
}

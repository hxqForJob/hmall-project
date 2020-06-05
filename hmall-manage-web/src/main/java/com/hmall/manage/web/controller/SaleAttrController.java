package com.hmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.BaseSaleAttr;
import com.hmall.pojo.SpuSaleAttr;
import com.hmall.service.BaseSaleAttrService;
import com.hmall.service.SpuSaleAtrrService;
import com.hmall.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.FormSubmitEvent;
import java.util.List;

//销售属性控制器
@RestController
@CrossOrigin
public class SaleAttrController {


    //注入基本销售属性业务逻辑
    @Reference
    private BaseSaleAttrService baseSaleAttrService;

    //注入商品销售属性业务逻辑
    @Reference
    private SpuSaleAtrrService spuSaleAtrrService;


    /**
     * 获取所有销售属性
     * @return
     */
    @RequestMapping(value = "/baseSaleAttrList")
   public List<BaseSaleAttr> getBaseSaleAttrList()
    {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrService.findAll();
        return  baseSaleAttrs;
    }

    @RequestMapping("/spuSaleAttrList")
    public  List<SpuSaleAttr> spuSaleAttrList(Integer spuId){
        return  spuSaleAtrrService.getSpuSaleAttrInfoBySpuId(spuId);
    }

}

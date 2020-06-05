package com.hmall.item.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hmall.pojo.SkuInfo;
import com.hmall.pojo.SkuSaleAttrValue;
import com.hmall.pojo.SpuSaleAttr;
import com.hmall.service.SkuInfoService;
import com.hmall.service.SkuLsService;
import com.hmall.service.SpuSaleAtrrService;
import com.hmall.web.utils.annotation.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    /**
     * 注入Sku业务逻辑
     */
    @Reference
    private SkuInfoService skuInfoService;

    /**
     * 注入销售属性业务逻辑
     */
    @Reference
    private SpuSaleAtrrService spuSaleAtrrService;

    /**
     * 注入SkuLs业务逻辑
     */
  @Reference
  private SkuLsService skuLsService;



    @RequestMapping("/{skuId}.html")
    public  String skuDetailUI(@PathVariable("skuId") Integer skuId, Model model){

        //添加访问量
        //skuLsService.addWatch(skuId);
        //获取SkuInfo和图片
        SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(skuId);
        if(skuInfo==null){
            return "redirect:http://www.hmall.com";
        }
        model.addAttribute("skuInfo",skuInfo);
        //获取当前sku下spu包含的销售属性和属性值，以及选中当前sku销售属性值
        List<SpuSaleAttr> spuSaleAttrs= spuSaleAtrrService.getSpuSaleAttrAndSelectValue(skuInfo.getSpuId(),skuInfo.getId());
        model.addAttribute("spuSaleAttrList",spuSaleAttrs);
        //获取当前sku对应spu的排列组合（{"销售属性值ID-销售属性值ID":"skuId"}）
        List<SkuSaleAttrValue> skuSaleAttrValueList=skuInfoService.getAllSkuSaleAttrValueCom(skuInfo.getSpuId());
        //System.out.println(skuSaleAttrValueList);
        String key="";
        Map<String,String> map=new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            key+=skuSaleAttrValueList.get(i).getSaleAttrValueId();
            String currentSkuId=skuSaleAttrValueList.get(i).getSkuId();
            //如果当前销售属性值
            if((i+1)!=skuSaleAttrValueList.size()&&currentSkuId.equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                key+="-";
            }else {
                map.put(key,currentSkuId);
                key="";
            }
        }
        String allSkuSaleAttrValueStr = JSON.toJSONString(map);
        model.addAttribute("skuInfoMapValue",allSkuSaleAttrValueStr);
        return "item";
    }
}

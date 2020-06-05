package com.hmall.list.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hmall.pojo.*;
import com.hmall.service.BaseAttrService;
import com.hmall.service.BaseSaleAttrService;
import com.hmall.service.SkuLsService;
import org.codehaus.groovy.runtime.ArrayUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
@CrossOrigin
public class SkuLsController {

    /**
     * 注入Sku列表业务逻辑
     */
    @Reference
    private SkuLsService skuLsService;


    /**
     * 注入平台属性业务逻辑
     */
    @Reference
    private BaseAttrService baseAttrService;

    @RequestMapping("list.html")
    public String getSkuLsResult(SkuLsParams skuLsParams, Model model){
        //返回前端关键字
        model.addAttribute("keyword",skuLsParams.getKeyword());
        //测试设置页大小为2
        //skuLsParams.setPageSize(2);
        //es查询
        SkuLsResult skuLsResult = skuLsService.getSkuLsResult(skuLsParams);
        //将sku基本信息传给前端
        model.addAttribute("skuLsInfoes",skuLsResult.getSkuLsInfoList());
        List<BaseAttrValueBread> baseAttrValueBreadList=new ArrayList<>();
        //获取查询的平台属性值Id
        String valueIds= StringUtils.join(skuLsResult.getAttrValueIdList().toArray(),",");
        //根据查询的SkuLsInfo获取平台属性和平台属性值
        List<BaseAttrInfo> baseAttrInfoList = baseAttrService.findByValueId(valueIds);
        //选中平台属性值后将该平台属性和平台属性值去掉
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0) {
            //遍历所有平台属性
            for (Iterator<BaseAttrInfo> baseAttrInfoIterator = baseAttrInfoList.iterator(); baseAttrInfoIterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = baseAttrInfoIterator.next();
                //遍历平台属性值
                for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
                    //遍历用户选中的平台属性值
                    for (String valueId : skuLsParams.getValueId()) {
                        //判断如果所有平台属性包含当前用户选中的平台属性则删除该平台属性
                        if(valueId.equals(baseAttrValue.getId())){
                            //实例化一个面包屑
                            BaseAttrValueBread baseAttrValueBread=new BaseAttrValueBread();
                            //拼接面包屑名称
                            String attrInfoAndVal=baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName();
                            baseAttrValueBread.setAttrInfo(attrInfoAndVal);
                            //拼接删除当前面包屑的urlParam
                            String breadUrlParam=makeUrlParamByCondition(skuLsParams,valueId);
                            baseAttrValueBread.setUrlParam(breadUrlParam);
                            //添加到面包屑集
                            baseAttrValueBreadList.add(baseAttrValueBread);
                            //删除平台属性
                            baseAttrInfoIterator.remove();
                            break;
                        }
                    }
                }

            }
        }
        //将平台属性传给前端
        model.addAttribute("baseAttrInfoList",baseAttrInfoList);
        //将面包屑传给前端
        model.addAttribute("breadList",baseAttrValueBreadList);
        //根据查询条件拼接当前条件组成的Url参数
        String urlParam=makeUrlParamByCondition(skuLsParams);
        //将新的Url参数回传给前端
        model.addAttribute("urlParam",urlParam);
        //分页Url参数
        String pageUrlParam=makePageUrlParamByCondition(skuLsParams);
        model.addAttribute("pageUrlParam",pageUrlParam);
        //将总页数回传给前端
        model.addAttribute("totalPages",skuLsResult.getTotalPages());
        //将当前页面回传给前端
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        //排序方式回传给前端
        model.addAttribute("orderBy",skuLsParams.getOrderBy());
        //价格排序方式，升序或降序
        model.addAttribute("owp",skuLsParams.getOwp());
        return  "list";
    }

    /**
     * 根据查询条件当前条件组成的Url参数
     * @param skuLsParams //查询条件参数
     * @param  valueIds//当前面包屑的属性值Id
     * @return
     */
    private String makeUrlParamByCondition(SkuLsParams skuLsParams,String...valueIds) {
        String urlParam="";
        //关键词不为空，拼接关键词
        if(!StringUtils.isEmpty(skuLsParams.getKeyword())){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        //分类Id不为空，拼接分类Id
        if(!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            if(!StringUtils.isEmpty(urlParam)){
                urlParam+="&catalog3Id="+skuLsParams.getCatalog3Id();
            }else {
                urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
            }
        }

        //判断平台属性值Id是否为空
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                //判断当前是否为面包屑
                if(valueIds!=null&&valueIds.length>0){
                    //如果为面包屑，取消拼接
                    if(valueId.equals(valueIds[0])){
                        continue;
                    }
                }
                if(!StringUtils.isEmpty(urlParam)){
                    urlParam+="&valueId="+valueId;
                }else {
                    urlParam+="valueId="+valueId;
                }
            }
        }
        return  urlParam;
    }

    /**
     * 制作分页url参数
     * @param skuLsParams
     * @return
     */
    private String makePageUrlParamByCondition(SkuLsParams skuLsParams) {
        String urlParam="";
        //关键词不为空，拼接关键词
        if(!StringUtils.isEmpty(skuLsParams.getKeyword())){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        //分类Id不为空，拼接分类Id
        if(!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            if(!StringUtils.isEmpty(urlParam)){
                urlParam+="&catalog3Id="+skuLsParams.getCatalog3Id();
            }else {
                urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
            }
        }
        //排序方式不为空，拼接排序方式
        if(!StringUtils.isEmpty(skuLsParams.getOrderBy())){
            if(skuLsParams.getOrderBy().equals("price")){
                if(!StringUtils.isEmpty(urlParam)){
                    urlParam+="&owp="+skuLsParams.getOwp();
                }else {
                    urlParam+="owp="+skuLsParams.getOwp();
                }
            }
            if(!StringUtils.isEmpty(urlParam)){
                urlParam+="&orderBy="+skuLsParams.getOrderBy();
            }else {
                urlParam+="orderBy="+skuLsParams.getOrderBy();
            }

        }
        //判断平台属性值Id是否为空
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                if(!StringUtils.isEmpty(urlParam)){
                    urlParam+="&valueId="+valueId;
                }else {
                    urlParam+="valueId="+valueId;
                }
            }
        }
        return  urlParam;
    }
}

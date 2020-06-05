package com.hmall.cart.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.CartInfo;
import com.hmall.pojo.SkuInfo;
import com.hmall.service.CartService;
import com.hmall.service.SkuInfoService;
import com.hmall.web.utils.CookieUtil;
import com.hmall.web.utils.annotation.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车控制器
 */
@Controller
public class CartController {

    //购物车的cookie键
private  static  final  String cartCookieKey="cart";

    /**
     * 购物车业务逻辑
     */
    @Reference
    private CartService cartService;

    /**
     * 注入cookie购物车业务逻辑
     */
    @Autowired
    private  CartCookieService cartCookieService;
    /**
     * sku业务逻辑
     */
    @Reference
    private SkuInfoService skuInfoService;

    /**
     * 添加购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/addToCart")
    @LoginRequire(autoRedirect = false)
    public  String addCart(HttpServletRequest request, HttpServletResponse response){
        String userId=request.getAttribute("userId")==null?null:request.getAttribute("userId").toString();
        String skuId=request.getParameter("skuId");
        String skuNum=request.getParameter("skuNum");
        if (skuNum==null||skuNum.length()<=0){
            skuNum="1";
        }
        if(userId!=null&&userId.length()>0){
            //已登录，添加到数据库
            cartService.addCartInfo(userId,skuId,Integer.valueOf(skuNum));
        }else {
            //未登录，添加到cookie中
            cartCookieService.addCartInfo(request,response,cartCookieKey,skuId,Integer.valueOf(skuNum));
        }
        //回显skuInfo和数量
        SkuInfo skuInfo=skuInfoService.getSkuInfoAndImageById(Integer.valueOf(skuId));
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    /**
     * 购物车列表
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response){
        String userId=request.getAttribute("userId")==null?null:request.getAttribute("userId").toString();
        Double totalPrice=0.0;
        List<CartInfo> cartInfoList=null;
        if(userId==null){
            //未登录，查询cookie
            cartInfoList= cartCookieService.cartList(request,cartCookieKey);
        }else {
            //登录合并购物车。
            //获取cookie购物车
            List<CartInfo> cookieCartList=cartCookieService.cartList(request,cartCookieKey);
            //获取数据数据库购物车
            List<CartInfo> userCartList=cartService.cartList(userId);
            //合并购物车
            cartInfoList=cartService.mergeCart(userCartList,cookieCartList,userId);
            //删除购物车cookie
            CookieUtil.deleteCookie(request,response,cartCookieKey);

        }
        if(cartInfoList!=null&&cartInfoList.size()>0){
            //计算总金额
            for (CartInfo cartInfo : cartInfoList) {
                //
                if("1".equals(cartInfo.getIsChecked())){
                    BigDecimal skuPrice = cartInfo.getSkuPrice();
                    totalPrice+=skuPrice.doubleValue()*cartInfo.getSkuNum();
                }
            }
        }
        request.setAttribute("cartList",cartInfoList);
        request.setAttribute("totalPrice",totalPrice);
        return "cartList";
    }


    /**
     * 购销购物车
     * @param skuId skuId
     * @param isChecked 是否勾选 "1"勾选，"0"未勾选
     */
    @RequestMapping("/checkCart")
    @LoginRequire(autoRedirect = false)
    public void  checkCart(String skuId,String isChecked,HttpServletRequest request,HttpServletResponse response){
        Object user = request.getAttribute("userId");
        if(user!=null){
            //已登录
            String userId=user.toString();

           cartService.checkedCart(userId,skuId,isChecked);
        }else {
            //未登录
            cartCookieService.checkedCart(request,response,skuId,isChecked,cartCookieKey);
        }
    }
}

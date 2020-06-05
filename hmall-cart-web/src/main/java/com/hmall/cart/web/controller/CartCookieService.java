package com.hmall.cart.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hmall.pojo.CartInfo;
import com.hmall.pojo.SkuInfo;
import com.hmall.service.SkuInfoService;
import com.hmall.web.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * cookie购物车业务逻辑
 */
@Component
public class CartCookieService {

    /**
     * sku业务逻辑
     */
    @Reference
    private SkuInfoService skuInfoService;

    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;


    /**
     * 添加购物车到cookie中
     * @param cartCookieKey
     * @param skuId
     */
    public void addCartInfo(HttpServletRequest request, HttpServletResponse response,String cartCookieKey, String skuId, Integer skuNum) {
        //定义购物车集合，用来保存购物车
        List<CartInfo> cartInfoLis=new ArrayList<>();
        //获取cookie中的购物车
        String cookieValue = CookieUtil.getCookieValue(request, cartCookieKey, true);
        //是否存在
        boolean isExits=false;
        if(cookieValue!=null&&cookieValue.length()>0){
            cartInfoLis= JSON.parseArray(cookieValue,CartInfo.class);
            if(cartInfoLis!=null&&cartInfoLis.size()>0){
                for (CartInfo cartInfo : cartInfoLis) {
                    //有相同的商品，修改数量
                    if(cartInfo.getSkuId().equals(skuId)){
                        //增加数量
                        cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                        isExits=true;
                        break;
                    }
                }
            }
        }
        //不存在，重新添加
        if(!isExits){
            //获取SkuInfo信息
            SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(Integer.valueOf(skuId));
            CartInfo cart=new CartInfo();
            cart.setSkuNum(skuNum);
            cart.setSkuId(skuId);
            cart.setCartPrice(BigDecimal.valueOf(skuInfo.getPrice()));
            cart.setImgUrl(skuInfo.getSkuDefaultImg());
            cart.setSkuName(skuInfo.getSkuName());
            cart.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
            cartInfoLis.add(cart);
        }
        //更新cookie
        CookieUtil.setCookie(request,response,cartCookieKey,JSON.toJSONString(cartInfoLis),COOKIE_CART_MAXAGE,true);
    }

    /**
     * 查询购物车
     * @param request
     * @return
     */
    public List<CartInfo> cartList(HttpServletRequest request,String cookieKey) {
        String cartCookie = CookieUtil.getCookieValue(request, cookieKey, true);
        if(cartCookie!=null&&cartCookie.length()>0){
            List<CartInfo> cartInfoList = JSON.parseArray(cartCookie, CartInfo.class);
            if(cartInfoList!=null&&cartInfoList.size()>0){
                //更新最新价格
                for (CartInfo cartInfo : cartInfoList) {
                    SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(Integer.valueOf(cartInfo.getSkuId()));
                    cartInfo.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                }
                return  cartInfoList;
            }
        }
        return  null;
    }

    /**
     * 选中和取消选中购物车
     * @param skuId
     * @param IsChecked
     */
    public void checkedCart(HttpServletRequest request,HttpServletResponse response, String skuId, String IsChecked,String cookieName) {
        String cartInfoStr = CookieUtil.getCookieValue(request, cookieName, true);
        List<CartInfo> cartInfoList=JSON.parseArray(cartInfoStr,CartInfo.class);
        if("1".equals(IsChecked)){
            //选中
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getSkuId())){
                    cartInfo.setIsChecked("1");
                }
            }

        }else {
            //取消选中
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getSkuId())){
                    cartInfo.setIsChecked("0");
                }
            }
        }
        CookieUtil.setCookie(request,response,cookieName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
    }


}

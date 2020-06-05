package com.hmall.passport.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.UserInfo;
import com.hmall.service.UserService;
import com.hmall.web.utils.CookieUtil;
import com.hmall.web.utils.JwtUtil;
import com.hmall.web.utils.WebConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证中心控制器
 */
@Controller
public class PassportController {

    /**
     * 注入用户业务逻辑
     */
    @Reference
    private UserService userService;

    /**
     * jwtkey
     */
    @Value("${jwt.key}")
    private  String jwtKey;



    /**
     * 登录页面
     * @param originUrl
     * @param model
     * @return
     */
    @RequestMapping("/index.html")
    public  String toLoginUI(String originUrl, Model model){
        if(StringUtils.isEmpty(originUrl)){
            originUrl="http://www.hmall.com";
        }
        model.addAttribute("originUrl",originUrl);
        return "index";
    }

    /**
     * 如果访问根路径直接跳转到首页
     * @return
     */
    @RequestMapping("/")
    public  String toIndex(){
        return "redirect:http://www.hmall.com";
    }

    /**
     * 登录接口
     * @param userInfo
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public  String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
      UserInfo user=  userService.login(userInfo);
      if(user!=null){
          //登录成功,生成token添加到cookie中
          Map<String,Object> userMap=new HashMap<>();
          userMap.put("userId",user.getId());
          userMap.put("nickName",user.getNickName());
          String jwtSalt=request.getParameter("X-forwarded-for");
          //生成token
          String token = JwtUtil.encode(jwtKey, userMap, jwtSalt);
          //存入cookie
          CookieUtil.setCookie(request,response,"uToken",token, WebConst.COOKIE_MAXAGE,false);
          return "success";
      }
        return "fail";
    }

    /**
     * 验证是否登录
     * @param token
     * @param  salt
     * @return
     */
    @RequestMapping("/verify")
    @ResponseBody
    public  boolean verify(String token,String salt){
        //token为空
        if(StringUtils.isEmpty(token)){
            return  false;
        }
        Map<String, Object> userMap = JwtUtil.decode(token, jwtKey, salt);
        //jwt解密为空
        if(userMap==null){
            return  false;
        }
        String userId=userMap.get("userId").toString();
       UserInfo userInfo= userService.getUserCacheById(userId);
       if(userInfo==null){
           return  false;
       }else {
           return  true;
       }
    }

    /**
     * 退出
     * @return
     */
    @RequestMapping("logout")
   public  String logout(HttpServletRequest request,HttpServletResponse response){
       //根据cookie查询userId,删除缓存数据
       String uToken = CookieUtil.getCookieValue(request,   "uToken", false);
        String jwtSalt=request.getParameter("X-forwarded-for");
       if(uToken!=null){
           Map<String, Object> uMap = JwtUtil.decode(uToken, jwtKey, jwtSalt);
           if(uMap!=null){
               String userId = uMap.get("userId").toString();
               //删除缓存
               userService.logout(userId);
           }
           //删除cookie
           CookieUtil.deleteCookie(request,response,"uToken");
       }
       return "redirect:"+WebConst.LOGIN_ADDRESS;
   }


}

package com.hmall.web.utils.intercept;

import com.hmall.web.utils.CookieUtil;
import com.hmall.utils.HttpClientUtil;
import com.hmall.web.utils.JwtUtil;
import com.hmall.web.utils.WebConst;
import com.hmall.web.utils.annotation.LoginRequire;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 登录拦截器
 */
@Component
public class AuthIntercepter extends HandlerInterceptorAdapter {

    private static  final String jwtKey="Hmall_Hxq.com";


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie中的token
        String token= CookieUtil.getCookieValue(request,"uToken",false);
        //从Nginx上获取访问的Ip地址
        String jwtSalt=request.getParameter("X-forwarded-for");
        if(token!=null){
            //解密，获取昵称
            Map<String, Object> uMap = JwtUtil.decode(token, jwtKey, jwtSalt);
            if(uMap!=null){
                //将昵称放入请求域中
                String nickName=uMap.get("nickName").toString();
                request.setAttribute("nickName",nickName);
            }
        }
        HandlerMethod handlerMethod= (HandlerMethod) handler;
        //获取控制器上标注LoginRequire的方法
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //如果方法含有注解，不为空且autoRedirect为true，说明此方法需要登录后才能访问
        if(methodAnnotation!=null){
            //获取当前控制器接口是否必须登录才能访问
            boolean flg = methodAnnotation.autoRedirect();
                //判断当前token是否为空
                if(token!=null&&token.length()>=0){
                    //有token、使用httpClient去认证中心查看是否登录
                    String verifyRes = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token+"?salt="+jwtSalt);
                    //认证成功
                    if(verifyRes.equals("true")){
                        //说明已登录,将userId保存到request域中
                        //解密token
                        Map<String, Object> uMap = JwtUtil.decode(token, jwtKey, jwtSalt);
                        if(uMap!=null){
                            String userId = uMap.get("userId").toString();
                            request.setAttribute("userId",userId);
                            return  true;
                        }
                    }
                }
            //认证失败,判断当前控制器接口是否必须登录
            if(flg){
                //未登录,重定向到登录页
                String  requestURL = request.getRequestURL().toString();
                if(requestURL.contains("http://order.hmall.com/toTrade")){
                    requestURL="http://cart.hmall.com/cartList";
                }
                String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                return  false;
            }
        }
        return  true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }
}

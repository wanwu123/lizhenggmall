package com.atguigu.gmall.intercepetor;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.constant.WebConst;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.atguigu.gmall.constant.WebConst.VERIFY_URL;

@Component
public class AuthIntercepetor extends HandlerInterceptorAdapter{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //检查Token
        String token = null;
        token = request.getParameter("newToken");
        if (token != null){//刚刚登录
            //把Token保存到Cookie中
            CookieUtil.setCookie(request,response,"token",token, WebConst.cookieMaxAge,false);
        }else {
            //从Cookie中取值
            token =  CookieUtil.getCookieValue(request,"token",false);
        }
        Map userMapFromToKen=null;
        if(token!= null){
            userMapFromToKen = getUserMapFromToKen(token);
            String nickName = (String)userMapFromToKen.get("nickName");
            request.setAttribute("nickName",nickName);
        }
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation !=null){
            //如果Token有用户信息那么取出
            if (token != null){
                //把Token放入认证中心进行认证
                String currentIp = request.getHeader("X-forwarded-for");
                String result = HttpClientUtil.doGet(VERIFY_URL + "?token=" + token + "&currentIp=" + currentIp);
                if ("success".equals(result)){
                    String userId = (String)userMapFromToKen.get("userId");
                    request.setAttribute("userId",userId);
                    return true;
                }else if (!methodAnnotation.autoRedirect()){//认证失败运行不跳转
                    return true;
                }else {//认证失败 强行跳转
                    redirect(request,response);
                }
            }else {
                if (!methodAnnotation.autoRedirect()){
                    return true;
                }else {
                    //跳转登录
                    redirect(request,response);
                    return false;
                }

            }
        }
        return true;
    }
    private void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String  requestURL = request.getRequestURL().toString();//取得用户当前登录请求
        String encodeURL = URLEncoder.encode(requestURL, "UTF-8");//编码
        response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);//重定向URL
    }
    private Map getUserMapFromToKen(String token){
        String tokenInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(tokenInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(decode,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }
}

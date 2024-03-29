package com.atguigu.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.service.UserService;
import com.atguigu.gmall.entity.UserInfo;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    String jwtKey = "atguigu";
    @Reference
    private UserService userService;
    @RequestMapping(value = "index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }
    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {
        UserInfo userInfo2 = userService.login(userInfo);
        if (userInfo2!=null){
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userInfo2.getId());
            map.put("nickName",userInfo2.getNickName());
            //配置Nginx
//            request.getRemoteAddr();
            String header = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(jwtKey, map, header);
            return token;
        }

        return "fail";
    }

    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam("token")String token,@RequestParam("currentIp")String currentIp ){
        //验证Token
        Map<String, Object> decode = JwtUtil.decode(token, jwtKey, currentIp);
        //验证缓存
        if (decode !=null){
            String userId =(String) decode.get("userId");
            UserInfo userInfo = userService.verfly(userId);
            if (userInfo != null){
                return "success";
            }
        }
        return "fail";
    }
    @Test
    public void testJWT(){
        Map<String, Object> map = new HashMap<>();
        map.put("userId","123");
        map.put("nickName","wanwu");
        String token = JwtUtil.encode("atguigu", map, "192.168.222.130");
        System.out.println(token);
        Map<String, Object> atguigu = JwtUtil.decode(token, "atguigu", "192.168.222.130");
        System.out.println(atguigu);

    }
}

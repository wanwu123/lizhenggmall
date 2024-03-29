package com.atguigu.gmall.config;

import com.atguigu.gmall.intercepetor.AuthIntercepetor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private AuthIntercepetor authIntercepetor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authIntercepetor).addPathPatterns("/**").addPathPatterns("/static/**").addPathPatterns("/templates/**");
    }

}

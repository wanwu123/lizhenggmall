package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class WxConfig {

    public final static String APPID="wxf913bfa3a2c7eeeb";
    public final static String PARTNER="1543338551";
    public final static String PARTNERKEY="atguigu3b0kn9g5v426MKfHQH7X8rKwb";
//    public WxConfig(){
//
//    }
//    public WxConfig(String appid,String partner,String partnerkey){
//        appid = this.app_id;
//        partner = this.partner;
//        partnerkey = this.partnerkey;
//    }
//    @Bean
//    public WxConfig wxConfig(){
//        WxConfig wxConfig =new WxConfig(app_id,partner,partnerkey);
//        return wxConfig;
//    }

}

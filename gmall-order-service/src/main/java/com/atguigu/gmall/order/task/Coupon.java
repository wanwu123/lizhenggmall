package com.atguigu.gmall.order.task;


import com.atguigu.gamll.service.OrderService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@EnableScheduling
public class Coupon {
    @Autowired
    private OrderService orderService;
//    @Scheduled(cron = "0 0 * * * ?")//每小时一次
//    @Scheduled(cron = "0 0/30 * * * ?")//半个小时
//    @Scheduled(cron = "5 * * * * ?")//每个分钟到5秒
//    
//    @Scheduled(cron = "0 30 2 21 * ?")//每月21
    @Scheduled(cron = "0/5 * * * * ?")//每5秒
    public void work() throws InterruptedException {
        System.out.println("--1"+Thread.currentThread());
        List<Integer> integers = orderService.checkExpiredCoupon();
        for (Integer integer : integers) {
            orderService.handleExpiredCoupon(integer);
        }
        Thread.sleep(10000);
    }
    /*@Scheduled(cron = "0/1 * * * * ?")//每1秒
    public void work2(){
        System.out.println("--2"+Thread.currentThread());
    }*/
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }

}

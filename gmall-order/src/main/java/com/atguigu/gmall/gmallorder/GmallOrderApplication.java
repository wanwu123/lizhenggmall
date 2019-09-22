package com.atguigu.gmall.gmallorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall")
public class GmallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallOrderApplication.class, args);
	}


}

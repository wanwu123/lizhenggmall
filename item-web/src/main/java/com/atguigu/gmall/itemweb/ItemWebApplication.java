package com.atguigu.gmall.itemweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@ComponentScan(basePackages = "com.atguigu.gmall")
@SpringBootApplication
public class ItemWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemWebApplication.class, args);
	}

}

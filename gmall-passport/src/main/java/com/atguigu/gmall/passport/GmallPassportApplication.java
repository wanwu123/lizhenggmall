package com.atguigu.gmall.passport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu.gmall")
public class GmallPassportApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPassportApplication.class, args);
	}

}

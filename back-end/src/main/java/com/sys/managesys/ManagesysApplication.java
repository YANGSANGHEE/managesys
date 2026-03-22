package com.sys.managesys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.sys.managesys.common.mapper")
@EnableScheduling
public class ManagesysApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManagesysApplication.class, args);
	}

}

package com.custody.custody_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CustodyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustodyServiceApplication.class, args);
	}

}

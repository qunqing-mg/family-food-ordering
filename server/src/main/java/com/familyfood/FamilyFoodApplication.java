package com.familyfood;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.familyfood.mapper")
public class FamilyFoodApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilyFoodApplication.class, args);
    }
}

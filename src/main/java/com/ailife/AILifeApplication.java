package com.ailife;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ailife.mapper")
public class AILifeApplication {

  public static void main(String[] args) {
    SpringApplication.run(AILifeApplication.class, args);
  }
}

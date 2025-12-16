package com.tech.novagraphbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.tech")
@MapperScan("com.tech.novagraphbackenduserservice.infrastructure.mapper")
@SpringBootApplication
public class NovagraphBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovagraphBackendUserServiceApplication.class, args);
    }

}

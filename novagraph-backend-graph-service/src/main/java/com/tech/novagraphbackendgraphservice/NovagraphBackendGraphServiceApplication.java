package com.tech.novagraphbackendgraphservice;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.tech")
@MapperScan("com.tech.novagraphbackendgraphservice.infrastructure.mapper")
@Import(RocketMQAutoConfiguration.class)
@SpringBootApplication
public class NovagraphBackendGraphServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovagraphBackendGraphServiceApplication.class, args);
    }

}

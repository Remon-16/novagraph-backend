package com.tech.novagraphbackendgraphservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.tech")
@SpringBootApplication
public class NovagraphBackendGraphServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovagraphBackendGraphServiceApplication.class, args);
    }

}

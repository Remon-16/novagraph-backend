package com.tech.novagraphbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.tech")
@SpringBootApplication
public class NovagraphBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovagraphBackendGatewayApplication.class, args);
    }

}

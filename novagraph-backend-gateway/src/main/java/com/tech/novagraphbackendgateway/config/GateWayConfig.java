package com.tech.novagraphbackendgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.socket.client.TomcatWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;


@Configuration
public class GateWayConfig {

    @Bean
    @Primary
    public RequestUpgradeStrategy requestUpgradeStrategy(){
        return new TomcatRequestUpgradeStrategy();
    }
    @Bean
    @Primary
    public WebSocketClient webSocketClient(){
        return new TomcatWebSocketClient();
    }
}

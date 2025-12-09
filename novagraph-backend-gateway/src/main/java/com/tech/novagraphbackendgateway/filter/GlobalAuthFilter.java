package com.tech.novagraphbackendgateway.filter;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private SecretKey secretKey;
    // 不需要验证token的路径
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/logout",
            "/api/doc.html",
            "/api/v3/api-docs",
            "/api/v2/api-docs",
            "/api/swagger-resources",
            "/api/swagger-ui.html",
            "/api/picture/tag_category",
            "/api/webjars/**"
    };

    private static final Map<String, String> IGNORE_LOGINS = new HashMap<>();

    static {
        IGNORE_LOGINS.put("/api/user/get/login", "/api/user/get/login");
    }
    /**
     * 判断是否为白名单路径
     * @param path 请求路径
     * @return 是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        for (String whitePath : WHITE_LIST) {
            if (antPathMatcher.match(whitePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String path = serverHttpRequest.getURI().getPath();
        // 判断路径中是否包含 inner，只允许内部调用
        if (antPathMatcher.match("/ **/inner/** ", path)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        }

        // 白名单路径直接放行
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 1. 如果是OPTIONS预检请求，直接放行
        if (HttpMethod.OPTIONS.matches(Objects.requireNonNull(serverHttpRequest.getMethod()).name())) {
            return chain.filter(exchange);
        }

        // 如果是 WebSocket 连接 跳过
        if (path.startsWith("/api/ws/")) {
            return chain.filter(exchange);
        }

        // 统一权限校验，通过 JWT 获取登录用户信息
        String token = serverHttpRequest.getHeaders().getFirst(JwtUtils.JWT_HEADER);

        // 放过不需要强制拥有登录态的方法
        if (StringUtils.isBlank(token) && IGNORE_LOGINS.containsKey(path)){
            return chain.filter(exchange);
        }

        // 如果请求头中没有token
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 如果token以Bearer 开头，则去掉前缀
        if (token.startsWith(JwtUtils.JWT_TOKEN_PREFIX)) {
            token = token.substring(JwtUtils.JWT_TOKEN_PREFIX.length());
        }

        // 验证token
        if (!JwtUtils.validateToken(token, secretKey)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");
        }
        try {
            // 从token中获取用户信息
            Long userId = JwtUtils.getUserIdFromToken(token, secretKey);
            String userAccount = JwtUtils.getUserAccountFromToken(token, secretKey);
            String userRole = JwtUtils.getUserRoleFromToken(token, secretKey);
            // 将用户信息添加到请求头中，传递给下游服务
            ServerHttpRequest mutableRequest = serverHttpRequest.mutate()
                    .header("userId", String.valueOf(userId))
                    .header("userAccount", userAccount)
                    .header("userRole", userRole)
                    .build();

            // 使用修改后的请求继续过滤器链
            return chain.filter(exchange.mutate().request(mutableRequest).build());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Token解析失败");
        }
    }

    /**
     * 优先级提到最高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}

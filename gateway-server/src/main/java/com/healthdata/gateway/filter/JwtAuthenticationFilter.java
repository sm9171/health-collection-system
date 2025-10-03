package com.healthdata.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("JWT Filter - Path: {}", request.getPath());

        String authHeader = request.getHeaders().getFirst("Authorization");

        log.info("JWT Filter - Authorization header: {}", authHeader != null ? "Present" : "Missing");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("JWT Filter - Invalid or missing Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            String recordKey = claims.get("recordKey", String.class);

            log.info("JWT Filter - Token valid. Email: {}, RecordKey: {}", email, recordKey);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-Record-Key", recordKey)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("JWT Filter - Token validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
package com.healthdata.gateway.config;

import com.healthdata.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.rewritePath("/api/users/(?<segment>.*)", "/${segment}"))
                        .uri("lb://user-service"))
                .route("health-data-service", r -> r.path("/api/health/**")
                        .filters(f -> f.rewritePath("/api/health/(?<segment>.*)", "/${segment}")
                                .filter(jwtAuthenticationFilter))
                        .uri("lb://health-data-service"))
                .build();
    }
}
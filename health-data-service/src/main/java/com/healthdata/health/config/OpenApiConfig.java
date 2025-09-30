package com.healthdata.health.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Health Data Service API")
                        .description("건강 활동 데이터 수집 및 조회를 담당하는 마이크로서비스 API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Health Data Collection System")
                                .email("admin@healthdata.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local Health Data Service"),
                        new Server().url("http://localhost:8080/api/health").description("Via Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
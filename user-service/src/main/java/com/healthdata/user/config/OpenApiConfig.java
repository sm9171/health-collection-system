package com.healthdata.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("사용자 관리 및 인증을 담당하는 마이크로서비스 API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Health Data Collection System")
                                .email("admin@healthdata.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local User Service"),
                        new Server().url("http://localhost:8080/api/users").description("Via Gateway")
                ));
    }
}
package com.kt.api_messaging_svc.config;

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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Messaging Service")
                        .description("KT API Messaging Service for SMS/Message management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KT Development Team")
                                .email("dev@kt.com")))
                .servers(List.of(
                        new Server().url("https://messaging-svc-a0euekhwgueqd7c0.koreacentral-01.azurewebsites.net").description("Local Development Server"),
                        new Server().url("https://api.kt.com").description("Production Server")
                ));
    }
}

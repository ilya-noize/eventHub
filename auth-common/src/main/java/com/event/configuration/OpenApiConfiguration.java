package com.event.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securitySchemesItem = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        Components components = new Components()
                .addSecuritySchemes("bearer-key", securitySchemesItem);
        Info description = new Info()
                .title("Auth API")
                .description("Документация по аутентификации");
        return new OpenAPI()
                .components(components)
                .info(description);
    }
}

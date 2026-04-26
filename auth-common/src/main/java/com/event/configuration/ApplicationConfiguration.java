package com.event.configuration;

import com.event.security.RestAccessDeniedHandler;
import com.event.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestAccessDeniedHandler restAccessDeniedHandler() {
        return new RestAccessDeniedHandler(this.objectMapper());
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthExceptionEntrypoint() {
        return new RestAuthenticationEntryPoint(this.objectMapper());
    }
}

package com.event.manager.configuration;

import com.auth.security.AuthorizationService;
import com.auth.security.JwtTokenFilter;
import com.auth.security.RestAccessDeniedHandler;
import com.auth.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableScheduling
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

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationService();
    }
}

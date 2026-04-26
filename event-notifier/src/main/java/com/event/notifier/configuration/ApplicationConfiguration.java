package com.event.notifier.configuration;

import com.event.security.AuthorizationService;
import com.event.security.JwtTokenFilter;
import com.event.security.RestAccessDeniedHandler;
import com.event.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableScheduling
@EnableJpaAuditing
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

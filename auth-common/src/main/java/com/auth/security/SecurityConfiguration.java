package com.auth.security;

import com.auth.domain.CustomUserDetailsService;
import com.event.common.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    private final UserDetailsService userDetailsService;
    private final JwtTokenFilter jwtTokenFilter;
    private final AccessDeniedHandler restAccessDeniedHandler;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfiguration(
            CustomUserDetailsService userDetailsService,
            JwtTokenFilter jwtTokenFilter,
            RestAccessDeniedHandler restAccessDeniedHandler,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) {
        security
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users/my").authenticated()//hasAuthority(UserRole.USER.name())
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtTokenFilter, AnonymousAuthenticationFilter.class)
                .exceptionHandling(handle -> handle
                        .accessDeniedHandler(restAccessDeniedHandler)
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                );

        return security.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

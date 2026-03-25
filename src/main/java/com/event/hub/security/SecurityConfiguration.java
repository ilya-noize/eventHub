package com.event.hub.security;

import com.event.hub.db.entity.UserRole;
import com.event.hub.exception.RestAccessDeniedHandler;
import com.event.hub.exception.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenFilter jwtTokenFilter;
    private final AccessDeniedHandler restAccessDeniedHandler;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfiguration(
            CustomUserDetailsService customUserDetailsService,
            JwtTokenFilter jwtTokenFilter,
            RestAccessDeniedHandler restAccessDeniedHandler,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) {
        String admin = UserRole.ADMIN.name();
        String user = UserRole.USER.name();
        security
            .formLogin(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/my").hasAuthority(user)
                .requestMatchers(HttpMethod.GET, "/users/**").hasAuthority(admin)

                .requestMatchers(HttpMethod.POST, "/locations").hasAuthority(admin)
                .requestMatchers(HttpMethod.PUT, "/locations/**").hasAuthority(admin)
                .requestMatchers(HttpMethod.PATCH, "/locations/**").hasAuthority(admin)
                .requestMatchers(HttpMethod.DELETE, "/locations/**").hasAuthority(admin)
                .requestMatchers("/locations/**").hasAnyAuthority(admin, user)

                .requestMatchers(HttpMethod.POST, "/events").hasAuthority(user)
                .requestMatchers(HttpMethod.POST, "/events/search").hasAnyAuthority(admin, user)
                .requestMatchers(HttpMethod.PUT, "/events/{eventId}").hasAnyAuthority(admin, user)
                .requestMatchers(HttpMethod.PATCH, "/events/{eventId}").hasAuthority(admin)
                .requestMatchers(HttpMethod.DELETE, "/events/{eventId}").hasAnyAuthority(admin, user)
                .requestMatchers(HttpMethod.GET, "/events/{eventId}").hasAnyAuthority(admin,user)
                .requestMatchers(HttpMethod.GET, "/events/my").hasAuthority(user)

                .requestMatchers(HttpMethod.POST, "/events/registrations/{eventId}").hasAuthority(user)
                .requestMatchers(HttpMethod.GET, "/events/registrations/cancel/{eventId}").hasAuthority(user)
                .requestMatchers(HttpMethod.GET, "/events/registrations/my").hasAuthority(user)

                .requestMatchers("/events/**").hasAnyAuthority(admin, user)

                .anyRequest().authenticated()
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
        var authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

package com.ftabah.giftme.adapter.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.ftabah.giftme.adapter.web.EmailProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, EmailProperties.class})
public class SecurityConfig {

    @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter,
                                                                                        RateLimitFilter rateLimitFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/", "/index.html", "/assets/**", "/favicon.ico",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api/auth/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, BearerTokenFilter.class)
                .build();
    }
}
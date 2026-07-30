package com.example.shipping.config;

import com.example.shipping.security.ApiKeyAuthenticationFilter;
import com.example.shipping.service.ApiKeyAuthenticationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiKeyProperties.class)
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    public ApiKeyAuthenticationService apiKeyAuthenticationService(ApiKeyProperties apiKeyProperties) {
        return new ApiKeyAuthenticationService(apiKeyProperties.getApiKeys());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, ApiKeyAuthenticationService apiKeyAuthenticationService) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers("/api/admin/rates").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(
                        new ApiKeyAuthenticationFilter(apiKeyAuthenticationService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

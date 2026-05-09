// ============================================================
// SecurityConfig.java - Seguridad del Product Service
// El catalogo de productos es publico — cualquiera puede
// ver los productos sin estar autenticado.
// Solo crear, actualizar y eliminar requieren token.
// ============================================================

package com.modex.productservice.config;

import com.modex.productservice.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // GET es publico — cualquiera puede ver productos
                        // sin estar autenticado
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**").permitAll()

                        // Actuator health es publico
                        .requestMatchers("/actuator/health").permitAll()

                        // Crear, actualizar y eliminar requieren token
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
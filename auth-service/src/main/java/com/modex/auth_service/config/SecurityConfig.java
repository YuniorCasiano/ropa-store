// ============================================================
// SecurityConfig.java - Configuracion de Spring Security
// Define las reglas de seguridad del Auth Service.
//
// Endpoints publicos — no necesitan token:
// POST /api/auth/register → para registrarse
// POST /api/auth/login    → para obtener el token
//
// Endpoints protegidos — necesitan token valido:
// POST /api/auth/refresh  → para renovar el token
// POST /api/auth/logout   → para cerrar sesion
//
// Tambien registra el JwtAuthenticationFilter en la cadena
// de filtros de Spring Security para que verifique el token
// en cada peticion entrante.
// ============================================================

package com.modex.auth_service.config;

import com.modex.auth_service.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // El filtro JWT que verifica el token en cada peticion.
    // Spring lo inyecta automaticamente.
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Desactivamos CSRF — no necesario en APIs REST con JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Configuracion de sesiones.
                // STATELESS significa que Spring Security NO guarda
                // sesiones en el servidor — cada peticion debe traer
                // su propio token JWT para autenticarse.
                // Esto es fundamental para microservicios — sin estado
                // en el servidor puedes escalar horizontalmente.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Reglas de autorizacion por endpoint
                .authorizeHttpRequests(auth -> auth

                        // Endpoints publicos — cualquiera puede acceder
                        // sin token. Son los endpoints para obtener el token.
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Actuator health — publico para que Docker
                        // pueda verificar si el servicio esta vivo
                        .requestMatchers("/actuator/health").permitAll()

                        // Todos los demas endpoints requieren autenticacion.
                        // Si la peticion no trae un JWT valido Spring
                        // devuelve HTTP 401 automaticamente.
                        .anyRequest().authenticated()
                )

                // Registramos nuestro filtro JWT en la cadena de filtros.
                // addFilterBefore significa "ejecuta JwtAuthenticationFilter
                // ANTES de UsernamePasswordAuthenticationFilter".
                // Asi nuestro filtro verifica el token antes de que
                // Spring Security intente autenticar por username/password.
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
// ============================================================
// SecurityConfig.java — Configuración temporal de seguridad
// Por ahora desactivamos la autenticación para poder probar
// los endpoints libremente con Postman.
//
// IMPORTANTE: Esta configuración es TEMPORAL.
// Cuando integremos el Auth Service con JWT, este archivo
// se actualizará para proteger los endpoints correctamente.
// ============================================================

package com.ropastore.user_service.config;

// Importaciones de Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration — Spring lee esta clase al arrancar
@Configuration

// @EnableWebSecurity — activa la configuración personalizada
// de Spring Security. Sin esto Spring usaría su configuración
// por defecto que bloquea todo.
@EnableWebSecurity
public class SecurityConfig {

    // @Bean — Spring crea y administra este objeto.
    // SecurityFilterChain define las reglas de seguridad
    // que Spring aplica a cada petición HTTP.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Desactivamos CSRF — Cross Site Request Forgery.
                // CSRF es una protección para aplicaciones web con
                // formularios HTML. En una API REST no es necesario
                // porque usamos tokens JWT para autenticación.
                .csrf(AbstractHttpConfigurer::disable)

                // Configuramos las reglas de autorización.
                // authorizeHttpRequests define qué endpoints
                // requieren autenticación y cuáles no.
                .authorizeHttpRequests(auth -> auth
                        // anyRequest().permitAll() — permite TODAS las
                        // peticiones sin autenticación.
                        // TEMPORAL — cuando agreguemos JWT cambiaremos
                        // esto por reglas específicas por endpoint.
                        .anyRequest().permitAll()
                );

        // build() construye y devuelve el SecurityFilterChain
        // con todas las reglas que definimos arriba.
        return http.build();
    }
}
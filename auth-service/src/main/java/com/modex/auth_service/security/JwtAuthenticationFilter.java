// ============================================================
// JwtAuthenticationFilter.java - Filtro de autenticacion JWT
// Es un filtro que Spring ejecuta en CADA peticion HTTP
// antes de que llegue al Controller.
//
// Lo que hace en cada peticion:
// 1. Lee el header Authorization
// 2. Extrae el token JWT
// 3. Verifica que el token sea valido
// 4. Si es valido registra al usuario como autenticado
//    en el contexto de Spring Security
// 5. La peticion continua al Controller
//
// Si el token es invalido o no existe la peticion continua
// sin autenticacion y Spring Security decide si permitirla
// segun las reglas del SecurityConfig.
// ============================================================

package com.modex.auth_service.security;

import com.modex.auth_service.repository.UserRepository;
import com.modex.auth_service.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

// OncePerRequestFilter garantiza que este filtro se ejecuta
// exactamente una vez por peticion — nunca dos veces.
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Usamos JwtUtil para extraer y validar el token
    private final JwtUtil jwtUtil;

    // Usamos UserRepository para verificar que el usuario existe
    private final UserRepository userRepository;

    // doFilterInternal es el metodo que Spring llama en cada peticion.
    // Parametros:
    // - request: la peticion HTTP entrante
    // - response: la respuesta HTTP que se enviara
    // - filterChain: la cadena de filtros — llamamos a
    //   filterChain.doFilter() para pasar al siguiente filtro
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Leemos el header Authorization de la peticion.
        // El cliente lo manda asi:
        // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        final String authHeader = request.getHeader("Authorization");

        // Si no hay header Authorization o no empieza con "Bearer "
        // simplemente pasamos al siguiente filtro sin autenticar.
        // Spring Security decidira si permitir la peticion
        // segun las reglas del SecurityConfig.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token — quitamos "Bearer " (7 caracteres)
        // "Bearer eyJhbGci..." → "eyJhbGci..."
        final String token = authHeader.substring(7);

        try {
            // Extraemos el email del payload del token
            final String email = jwtUtil.extractEmail(token);

            // Solo procesamos si tenemos email y el usuario
            // aun no esta autenticado en este contexto.
            // SecurityContextHolder.getContext().getAuthentication()
            // devuelve null si el usuario no esta autenticado.
            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                // Verificamos que el usuario existe en MongoDB
                // y que el token es valido para ese usuario
                boolean userExists = userRepository
                        .existsByEmail(email);

                if (userExists && jwtUtil.isTokenValid(token, email)) {

                    // Creamos un objeto UserDetails que Spring
                    // Security entiende — con el email como username,
                    // password vacia y sin roles por ahora.
                    UserDetails userDetails = User.builder()
                            .username(email)
                            .password("")
                            .authorities(new ArrayList<>())
                            .build();

                    // Creamos el token de autenticacion de Spring Security.
                    // UsernamePasswordAuthenticationToken representa
                    // un usuario autenticado en el contexto de Spring.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Agregamos detalles de la peticion al token
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Registramos al usuario como autenticado
                    // en el contexto de Spring Security.
                    // A partir de aqui Spring sabe quien es el usuario
                    // y permite el acceso a endpoints protegidos.
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Usuario autenticado via JWT: {}", email);
                }
            }
        } catch (Exception e) {
            // Si hay cualquier error procesando el token
            // simplemente continuamos sin autenticar.
            // El GlobalExceptionHandler no captura errores
            // de filtros — los manejamos aqui.
            log.debug("Error procesando JWT: {}", e.getMessage());
        }

        // Pasamos la peticion al siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}
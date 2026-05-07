// ============================================================
// AuthController.java - Endpoints del Auth Service
// Define las URLs de autenticacion de Modex.
//
// Endpoints:
// POST /api/auth/register → registrar usuario nuevo
// POST /api/auth/login    → iniciar sesion
// POST /api/auth/refresh  → renovar access token
// POST /api/auth/logout   → cerrar sesion
// ============================================================

package com.modex.auth_service.controller;

import com.modex.auth_service.dto.AuthResponseDTO;
import com.modex.auth_service.dto.LoginRequestDTO;
import com.modex.auth_service.dto.RefreshTokenRequestDTO;
import com.modex.auth_service.dto.RegisterRequestDTO;
import com.modex.auth_service.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── ENDPOINT 1: Register ──────────────────────────────────
    // POST /api/auth/register
    // Registra un usuario nuevo en Modex.
    // No requiere token — es publico en SecurityConfig.
    //
    // Ejemplo de request body:
    // {
    //   "fullName": "Juan Perez",
    //   "email": "juan@gmail.com",
    //   "password": "miPassword123",
    //   "city": "Santo Domingo",
    //   "country": "Republica Dominicana"
    // }
    //
    // Responde con HTTP 201 Created y los tokens generados.
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto) {

        log.info("POST /api/auth/register - Registrando: {}",
                dto.email());

        AuthResponseDTO response = authService.register(dto);

        // HTTP 201 Created — se creo un recurso nuevo
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ── ENDPOINT 2: Login ─────────────────────────────────────
    // POST /api/auth/login
    // Autentica un usuario y devuelve los tokens JWT.
    // No requiere token — es publico en SecurityConfig.
    //
    // Ejemplo de request body:
    // {
    //   "email": "juan@gmail.com",
    //   "password": "miPassword123"
    // }
    //
    // Responde con HTTP 200 OK y los tokens generados.
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        log.info("POST /api/auth/login - Login para: {}",
                dto.email());

        AuthResponseDTO response = authService.login(dto);

        // HTTP 200 OK — autenticacion exitosa
        return ResponseEntity.ok(response);
    }

    // ── ENDPOINT 3: Refresh ───────────────────────────────────
    // POST /api/auth/refresh
    // Genera un nuevo access token usando el refresh token.
    // Requiere que el cliente este autenticado — el refresh
    // token actua como credencial aqui.
    //
    // Ejemplo de request body:
    // {
    //   "refreshToken": "a1b2c3d4e5f6..."
    // }
    //
    // Responde con HTTP 200 OK y el nuevo access token.
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {

        log.info("POST /api/auth/refresh - Renovando token");

        AuthResponseDTO response = authService.refreshToken(dto);

        return ResponseEntity.ok(response);
    }

    // ── ENDPOINT 4: Logout ────────────────────────────────────
    // POST /api/auth/logout
    // Cierra la sesion del usuario revocando su refresh token.
    //
    // Ejemplo de request body:
    // {
    //   "refreshToken": "a1b2c3d4e5f6..."
    // }
    //
    // Responde con HTTP 204 No Content — operacion exitosa
    // sin cuerpo de respuesta.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {

        log.info("POST /api/auth/logout - Cerrando sesion");

        authService.logout(dto.refreshToken());

        // HTTP 204 No Content — operacion exitosa sin body
        return ResponseEntity.noContent().build();
    }
}
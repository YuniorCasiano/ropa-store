// ============================================================
// RefreshTokenRequestDTO.java - DTO de solicitud de refresh
// Define que manda el cliente cuando su access token expiro
// y necesita uno nuevo sin hacer login de nuevo.
//
// Flujo:
// 1. Cliente hace una peticion con su access token
// 2. El servidor responde 401 Unauthorized - token expirado
// 3. Cliente manda el refresh token a POST /api/auth/refresh
// 4. Auth Service verifica el refresh token en MongoDB
// 5. Si es valido genera un nuevo access token
// 6. Cliente usa el nuevo access token
//
// Ejemplo de JSON que recibe:
// {
//   "refreshToken": "a1b2c3d4e5f6g7h8i9j0..."
// }
// ============================================================

package com.modex.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(

        // El refresh token que el cliente guardo cuando hizo login.
        // El Auth Service lo busca en MongoDB, verifica que no
        // haya expirado ni sido revocado y genera un nuevo
        // access token si todo esta correcto.
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken

) {}
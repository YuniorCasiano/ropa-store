// ============================================================
// AuthResponseDTO.java - DTO de respuesta de autenticacion
// Define exactamente que devuelve el Auth Service despues
// de un login o registro exitoso.
//
// El cliente guarda estos tokens y los usa asi:
// - accessToken: lo manda en cada peticion en el header
//   Authorization: Bearer eyJhbGci...
// - refreshToken: lo guarda de forma segura y lo usa
//   cuando el accessToken expira para obtener uno nuevo
//
// Ejemplo de JSON que devuelve:
// {
//   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
//   "refreshToken": "a1b2c3d4e5f6...",
//   "tokenType": "Bearer",
//   "expiresIn": 3600000,
//   "email": "juan@gmail.com",
//   "fullName": "Juan Perez"
// }
// ============================================================

package com.modex.auth_service.dto;

public record AuthResponseDTO(

        // El token de acceso JWT — el cliente lo manda en
        // cada peticion en el header Authorization.
        // Expira en 1 hora (configurado en application.properties)
        String accessToken,

        // El token de refresco — el cliente lo guarda de forma
        // segura y lo usa cuando el accessToken expira.
        // Expira en 7 dias (configurado en application.properties)
        String refreshToken,

        // El tipo de token — siempre es "Bearer" para JWT.
        // El cliente lo usa para armar el header:
        // Authorization: Bearer {accessToken}
        String tokenType,

        // Cuanto tiempo dura el accessToken en milisegundos.
        // El cliente lo usa para saber cuando debe renovar el token
        // sin esperar a que el servidor lo rechace.
        // Valor: 3600000 = 1 hora
        Long expiresIn,

        // Email del usuario autenticado.
        // El cliente lo usa para mostrar informacion del usuario
        // sin tener que hacer otra peticion al User Service.
        String email,

        // Nombre completo del usuario.
        // Util para mostrar "Bienvenido Juan" en el frontend
        // inmediatamente despues del login sin peticiones extra.
        String fullName

) {
    // Factory method — forma conveniente de crear un AuthResponseDTO.
    // Se usa asi:
    // AuthResponseDTO.of(accessToken, refreshToken, 3600000L, email, fullName)
    public static AuthResponseDTO of(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String email,
            String fullName) {

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",    // tokenType siempre es Bearer
                expiresIn,
                email,
                fullName
        );
    }
}
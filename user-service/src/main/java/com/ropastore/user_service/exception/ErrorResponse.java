// ============================================================
// ErrorResponse.java — Forma del JSON de error
// Define exactamente cómo se ve un error en la respuesta HTTP.
// Es un DTO pero para errores — estructura consistente en
// todos los errores de la API.
//
// Ejemplo de respuesta que genera:
// {
//   "status": 404,
//   "error": "Not Found",
//   "message": "No existe usuario con el id: abc123",
//   "path": "/api/users/abc123",
//   "timestamp": "2024-01-15T10:30:00"
// }
// ============================================================

package com.ropastore.user_service.exception;

import java.time.LocalDateTime;

// Record — inmutable, perfecto para respuestas de error
// que no necesitan modificarse después de crearse.
public record ErrorResponse(

        // Código HTTP numérico — 404, 400, 409, 500, etc.
        int status,

        // Nombre del error HTTP — "Not Found", "Bad Request", etc.
        String error,

        // Mensaje específico del error — lo que realmente pasó.
        // Ejemplo: "No existe usuario con el id: abc123"
        // Este es el mensaje más útil para el cliente.
        String message,

        // El endpoint donde ocurrió el error.
        // Ejemplo: "/api/users/abc123"
        // Útil para depurar cuando tienes muchos endpoints.
        String path,

        // Fecha y hora exacta del error.
        // Útil para correlacionar con los logs del servidor.
        LocalDateTime timestamp

) {
    // Factory method — forma conveniente de crear un ErrorResponse
    // sin tener que pasar el timestamp manualmente cada vez.
    // Se usa así: ErrorResponse.of(404, "Not Found", "mensaje", "/path")
    public static ErrorResponse of(int status, String error,
                                   String message, String path) {
        return new ErrorResponse(
                status,
                error,
                message,
                path,
                LocalDateTime.now()  // Timestamp automático
        );
    }
}
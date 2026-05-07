// ============================================================
// ErrorResponse.java - Forma del JSON de error
// Identico al del User Service — estructura consistente
// en todos los errores de la API de Modex.
// ============================================================

package com.modex.auth_service.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error,
                                   String message, String path) {
        return new ErrorResponse(status, error, message,
                path, LocalDateTime.now());
    }
}
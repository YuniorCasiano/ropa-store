// ============================================================
// AuthException.java - Excepcion de autenticacion
// Se lanza cuando algo falla en el proceso de autenticacion.
// Ejemplos:
// - Credenciales incorrectas en el login
// - Refresh token invalido o expirado
// - Email duplicado en el registro
// - Usuario inactivo intentando hacer login
//
// El GlobalExceptionHandler la convierte en HTTP 401
// Unauthorized o HTTP 409 Conflict segun el caso.
// ============================================================

package com.modex.auth_service.exception;

public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // Constructor con mensaje personalizado
    // Se usa asi: throw new AuthException("Credenciales incorrectas")
    public AuthException(String message) {
        super(message);
    }
}
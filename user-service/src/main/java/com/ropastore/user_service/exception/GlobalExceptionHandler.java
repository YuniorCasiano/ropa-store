// ============================================================
// GlobalExceptionHandler.java — Manejador global de errores
// Captura TODAS las excepciones del proyecto en un solo lugar
// y las convierte en respuestas HTTP con formato consistente.
//
// Sin esta clase cada excepción mostraría un error técnico
// feo de Java. Con esta clase todas las respuestas de error
// tienen el mismo formato JSON profesional.
//
// Funciona así:
// 1. El Service lanza una excepción (ej: UserNotFoundException)
// 2. Spring la intercepta antes de llegar al cliente
// 3. Spring busca el @ExceptionHandler correspondiente aquí
// 4. Este método crea un ErrorResponse y lo devuelve al cliente
// ============================================================

package com.ropastore.user_service.exception;

// Importaciones de Spring para manejo de errores HTTP
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

// Importaciones de Lombok
import lombok.extern.slf4j.Slf4j;

// Importaciones de Java
import java.util.HashMap;
import java.util.Map;

// @Slf4j — genera el objeto log automáticamente para
// registrar todos los errores que ocurran.
@Slf4j

// @RestControllerAdvice — combina @ControllerAdvice y @ResponseBody.
// Le dice a Spring "esta clase maneja excepciones de forma global
// y sus métodos devuelven JSON automáticamente".
// Spring la detecta al arrancar y la registra como interceptor
// de excepciones para todos los Controllers.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── HANDLER 1: Usuario no encontrado ─────────────────────
    // Captura UserNotFoundException y devuelve HTTP 404.
    // Se activa cuando el Service lanza:
    // throw new UserNotFoundException(id)
    //
    // @ExceptionHandler — le dice a Spring qué tipo de excepción
    // maneja este método. Cuando Spring detecta una
    // UserNotFoundException en cualquier parte del código,
    // ejecuta este método automáticamente.
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            WebRequest request) {

        // Registramos el error en los logs para poder rastrearlo
        log.warn("Usuario no encontrado: {}", ex.getMessage());

        // Creamos la respuesta de error con el factory method
        // que definimos en ErrorResponse.
        // request.getDescription(false) extrae el path del request
        // en formato "uri=/api/users/abc123" — quitamos "uri=" con replace
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),        // 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // "Not Found"
                ex.getMessage(),                     // "No existe usuario con el id: abc123"
                request.getDescription(false).replace("uri=", "")
        );

        // ResponseEntity.status(404).body(error) construye la
        // respuesta HTTP completa con código 404 y el JSON del error.
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    // ── HANDLER 2: Email duplicado ────────────────────────────
    // Captura IllegalArgumentException y devuelve HTTP 409 Conflict.
    // Se activa cuando el Service lanza:
    // throw new IllegalArgumentException("Ya existe un usuario...")
    //
    // HTTP 409 Conflict significa "el recurso que intentas crear
    // entra en conflicto con uno que ya existe" — perfecto para
    // emails duplicados.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),            // 409
                HttpStatus.CONFLICT.getReasonPhrase(),  // "Conflict"
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // ── HANDLER 3: Validación de campos ──────────────────────
    // Captura MethodArgumentNotValidException — se lanza
    // automáticamente cuando los campos del DTO no pasan
    // las validaciones (@NotBlank, @Email, @Size).
    //
    // Ejemplo: si el cliente manda un JSON sin email,
    // Spring lanza esta excepción antes de llegar al Service.
    // Devolvemos HTTP 400 Bad Request con todos los errores
    // de validación en un mapa campo → mensaje de error.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Error de validación: {}", ex.getMessage());

        // Creamos un mapa con todos los errores de validación.
        // Clave = nombre del campo que falló.
        // Valor = mensaje de error de la anotación.
        // Ejemplo: { "email": "El formato del email no es válido" }
        Map<String, String> fieldErrors = new HashMap<>();

        // getBindingResult().getAllErrors() devuelve todos los
        // campos que fallaron la validación.
        ex.getBindingResult().getAllErrors().forEach(err -> {
            // Casteamos a FieldError para obtener el nombre del campo
            String fieldName = ((FieldError) err).getField();
            // getDefaultMessage() devuelve el mensaje que pusiste
            // en la anotación: @NotBlank(message = "El email es obligatorio")
            String message = err.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        // Construimos la respuesta con el mapa de errores
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());  // 400
        response.put("error", "Validation Failed");
        response.put("fields", fieldErrors);  // Mapa con todos los errores
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ── HANDLER 4: Error genérico ─────────────────────────────
    // Captura cualquier excepción que no fue capturada por
    // los handlers anteriores. Es el "catch-all" — el último
    // recurso para que nunca llegue un error técnico feo al cliente.
    //
    // Devuelve HTTP 500 Internal Server Error.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        // Para errores inesperados usamos log.error — son más
        // graves que los warn y necesitan atención inmediata.
        log.error("Error inesperado: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),        // 500
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), // "Internal Server Error"
                "Ocurrió un error inesperado. Por favor intenta de nuevo.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
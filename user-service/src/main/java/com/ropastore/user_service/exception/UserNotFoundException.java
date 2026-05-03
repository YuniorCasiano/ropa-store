// ============================================================
// UserNotFoundException.java — Excepción de usuario no encontrado
// Se lanza cuando buscas un usuario por ID o email y no existe
// en MongoDB.
//
// Extiende RuntimeException — esto significa que es una
// excepción no verificada (unchecked). No necesitas declarar
// "throws UserNotFoundException" en cada método que la use.
// Spring la captura automáticamente con @ControllerAdvice.
//
// El GlobalExceptionHandler la convierte en HTTP 404.
// ============================================================

package com.ropastore.user_service.exception;

// RuntimeException es la clase base de todas las excepciones
// no verificadas en Java. Al extenderla creamos nuestra propia
// excepción personalizada con un mensaje específico.
public class UserNotFoundException extends RuntimeException {

    // serialVersionUID es requerido por Java cuando una clase
    // es Serializable (RuntimeException lo es).
    // Es un identificador de versión — lo dejamos en 1L.
    private static final long serialVersionUID = 1L;

    // Constructor 1 — recibe el ID del usuario no encontrado.
    // Genera un mensaje claro: "No existe usuario con el id: abc123"
    // Lo usamos cuando buscamos por ID.
    //
    // super() llama al constructor de RuntimeException
    // pasándole el mensaje — así Spring puede leerlo después.
    public UserNotFoundException(String id) {
        super("No existe usuario con el id: " + id);
    }

    // Constructor 2 — recibe un mensaje personalizado completo.
    // Lo usamos cuando buscamos por email u otras condiciones
    // donde el mensaje necesita ser más específico.
    // Ejemplo: "No existe usuario con el email: juan@gmail.com"
    public UserNotFoundException(String field, String value) {
        super("No existe usuario con " + field + ": " + value);
    }
}
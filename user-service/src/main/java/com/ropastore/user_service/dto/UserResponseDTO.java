// ============================================================
// UserResponseDTO.java — DTO de respuesta de usuario
// Define exactamente qué datos devuelve la API cuando
// alguien consulta un usuario.
//
// Regla de oro: NUNCA incluir la contraseña en la respuesta.
// Aunque el Model User.java tiene el campo password,
// este DTO simplemente no lo incluye — así es imposible
// que la contraseña salga accidentalmente por HTTP.
//
// Este DTO se usa en todas las respuestas del UserController:
// - GET  /api/users        → devuelve List<UserResponseDTO>
// - GET  /api/users/{id}   → devuelve UserResponseDTO
// - POST /api/users        → devuelve UserResponseDTO
// - PUT  /api/users/{id}   → devuelve UserResponseDTO
//
// Ejemplo de JSON que devuelve:
// {
//   "id": "64f1a2b3c4d5e6f7a8b9c0d1",
//   "fullName": "Juan Pérez",
//   "email": "juan@gmail.com",
//   "phoneNumber": "8091234567",
//   "shippingAddress": "Calle Principal #123",
//   "city": "Santo Domingo",
//   "country": "República Dominicana",
//   "active": true,
//   "createdAt": "2024-01-15T10:30:00",
//   "updatedAt": "2024-01-15T10:30:00"
// }
// ============================================================

package com.ropastore.user_service.dto;

// Importamos LocalDateTime para representar las fechas
// de creación y modificación del usuario.
import java.time.LocalDateTime;

public record UserResponseDTO(

        // El ID generado por MongoDB.
        // El cliente lo necesita para hacer operaciones
        // futuras como GET /api/users/{id} o hacer un pedido.
        String id,

        // Nombre completo del usuario.
        String fullName,

        // Email del usuario.
        // Se incluye en la respuesta porque el usuario
        // necesita saber con qué email está registrado.
        String email,

        // Teléfono — puede ser null si el usuario no lo agregó.
        // Recuerda que en application.properties configuramos
        // spring.jackson.default-property-inclusion=non_null
        // entonces si es null, Jackson simplemente no lo incluye
        // en el JSON de respuesta — la respuesta queda más limpia.
        String phoneNumber,

        // Dirección de envío — puede ser null.
        String shippingAddress,

        // Ciudad — puede ser null.
        String city,

        // País — puede ser null.
        String country,

        // Estado de la cuenta.
        // true  = cuenta activa, puede hacer pedidos.
        // false = cuenta desactivada, no puede hacer pedidos.
        Boolean active,

        // Fecha exacta en que se creó la cuenta.
        // Spring la llenó automáticamente gracias a @CreatedDate
        // y MongoConfig.java que habilitamos ayer.
        // Formato en JSON: "2024-01-15T10:30:00"
        LocalDateTime createdAt,

        // Fecha de la última modificación.
        // Se actualiza automáticamente con @LastModifiedDate
        // cada vez que se guarda el documento en MongoDB.
        LocalDateTime updatedAt

        // NOTA: el campo password del Model User.java
        // NO está aquí — esa es la protección.
        // Es imposible que salga en la respuesta HTTP.

) {}
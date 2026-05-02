// ============================================================
// UpdateUserDTO.java — DTO de actualización de usuario
// Define qué datos puede modificar un usuario existente.
// Es lo que llega en el body del PUT /api/users/{id}
//
// Importante: NO incluye email ni password.
// - El email no se puede cambiar una vez registrado
//   porque es el identificador único del usuario.
// - La password tiene su propio endpoint seguro
//   que requiere verificar la contraseña actual primero.
//
// Todos los campos son opcionales — el usuario puede
// actualizar solo lo que quiera cambiar.
//
// Ejemplo de JSON que recibe:
// {
//   "fullName": "Juan Carlos Pérez",
//   "phoneNumber": "8097654321",
//   "shippingAddress": "Av. Winston Churchill #456",
//   "city": "Santiago",
//   "country": "República Dominicana"
// }
// ============================================================

package com.ropastore.user_service.dto;

// Importaciones de validación
import jakarta.validation.constraints.Size;

public record UpdateUserDTO(

        // Nombre completo — opcional en la actualización.
        // Si el cliente no lo manda, no se modifica.
        // @Size valida que si lo manda, tenga entre 2 y 100 caracteres.
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String fullName,

        // Teléfono — opcional.
        // El usuario puede agregar o cambiar su teléfono.
        String phoneNumber,

        // Dirección de envío — opcional.
        // El usuario puede cambiar dónde quiere recibir sus pedidos.
        String shippingAddress,

        // Ciudad — opcional.
        String city,

        // País — opcional.
        String country

) {}
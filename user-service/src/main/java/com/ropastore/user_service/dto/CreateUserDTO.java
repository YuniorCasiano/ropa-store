// ============================================================
// CreateUserDTO.java — DTO de creación de usuario
// Define exactamente qué datos debe mandar el cliente
// cuando quiere crear un usuario nuevo.
// Es lo que llega en el body del POST /api/users
//
// Ejemplo de JSON que recibe:
// {
//   "fullName": "Juan Pérez",
//   "email": "juan@gmail.com",
//   "password": "miPassword123",
//   "phoneNumber": "8091234567",
//   "shippingAddress": "Calle Principal #123",
//   "city": "Santo Domingo",
//   "country": "República Dominicana"
// }
// ============================================================

package com.ropastore.user_service.dto;

// Importaciones de validación
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Un record en Java es una clase especial para transportar datos.
// Esta línea reemplaza toda una clase con constructor, getters,
// equals, hashCode y toString — Java lo genera todo automáticamente.
//
// Cada parámetro entre paréntesis es un campo del DTO.
// Son inmutables — una vez recibidos no se pueden modificar.
public record CreateUserDTO(

        // Nombre completo — obligatorio
        // @NotBlank: no puede ser null, vacío ni solo espacios
        // @Size: entre 2 y 100 caracteres
        // message: el texto del error si la validación falla
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String fullName,

        // Email — obligatorio y con formato válido
        // @Email: valida que tenga el formato usuario@dominio.com
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        // Contraseña — obligatoria con mínimo 8 caracteres
        // Aquí llega en texto plano — el Service la encriptará
        // con BCrypt antes de guardarla en MongoDB.
        // Nunca guardamos contraseñas en texto plano.
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        String password,

        // Teléfono — opcional, por eso no tiene @NotBlank
        // El usuario puede registrarse sin teléfono y agregarlo después
        String phoneNumber,

        // Dirección de envío — opcional al registrarse
        // El usuario puede agregarla cuando haga su primer pedido
        String shippingAddress,

        // Ciudad — opcional
        String city,

        // País — opcional
        String country

) {}
// Las llaves vacías al final son obligatorias en un record.
// Aquí podrías agregar métodos personalizados si los necesitaras,
// pero para un DTO de entrada no son necesarios.
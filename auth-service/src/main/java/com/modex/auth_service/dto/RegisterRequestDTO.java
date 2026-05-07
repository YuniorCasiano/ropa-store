// ============================================================
// RegisterRequestDTO.java - DTO de solicitud de registro
// Define que datos manda el cliente cuando quiere crear
// una cuenta nueva en Modex.
//
// Ejemplo de JSON que recibe:
// {
//   "fullName": "Juan Perez",
//   "email": "juan@gmail.com",
//   "password": "miPassword123",
//   "phoneNumber": "8091234567",
//   "shippingAddress": "Calle Principal #123",
//   "city": "Santo Domingo",
//   "country": "Republica Dominicana"
// }
//
// Este DTO es casi identico al CreateUserDTO del User Service
// porque el Auth Service reenvía estos datos al User Service
// para crear el usuario en MongoDB.
// ============================================================

package com.modex.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        // Nombre completo — obligatorio
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String fullName,

        // Email — obligatorio y unico en el sistema
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es valido")
        String email,

        // Password — obligatoria con minimo 8 caracteres
        // El Auth Service la encriptara con BCrypt antes
        // de mandarla al User Service para guardar.
        @NotBlank(message = "La password es obligatoria")
        @Size(min = 8, message = "La password debe tener minimo 8 caracteres")
        String password,

        // Telefono — opcional
        String phoneNumber,

        // Direccion de envio — opcional
        String shippingAddress,

        // Ciudad — opcional
        String city,

        // Pais — opcional
        String country

) {}
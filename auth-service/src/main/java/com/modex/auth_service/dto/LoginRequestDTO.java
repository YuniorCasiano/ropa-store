// ============================================================
// LoginRequestDTO.java - DTO de solicitud de login
// Define exactamente que datos manda el cliente cuando
// quiere iniciar sesion.
//
// Ejemplo de JSON que recibe:
// {
//   "email": "juan@gmail.com",
//   "password": "miPassword123"
// }
//
// Solo necesitamos email y password para autenticar.
// Nada mas — el sistema busca el usuario por email y
// verifica la password con BCrypt.
// ============================================================

package com.modex.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        // Email del usuario — obligatorio y con formato valido
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es valido")
        String email,

        // Password — obligatoria
        // No validamos longitud aqui porque si el usuario
        // existe en la base de datos su password ya cumplio
        // las reglas cuando se registro.
        @NotBlank(message = "La password es obligatoria")
        String password

) {}
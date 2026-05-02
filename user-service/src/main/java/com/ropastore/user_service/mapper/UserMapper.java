// ============================================================
// UserMapper.java — Conversor entre Model y DTOs
// Tiene una sola responsabilidad: convertir objetos.
// Convierte User → UserResponseDTO y
// Convierte CreateUserDTO → User
//
// Todos los métodos son estáticos — no necesitas crear
// una instancia de esta clase para usarlos.
// Se usan así: UserMapper.toResponseDTO(user)
// ============================================================

package com.ropastore.user_service.mapper;

// Importamos las clases que vamos a convertir
import com.ropastore.user_service.dto.CreateUserDTO;
import com.ropastore.user_service.dto.UpdateUserDTO;
import com.ropastore.user_service.dto.UserResponseDTO;
import com.ropastore.user_service.model.User;

public class UserMapper {

    // Constructor privado — evita que alguien cree una instancia
    // de esta clase con "new UserMapper()".
    // Como todos los métodos son estáticos, nunca necesitas
    // una instancia. El constructor privado lo deja claro.
    private UserMapper() {}

    // ── MÉTODO 1: User → UserResponseDTO ─────────────────────
    // Convierte un documento de MongoDB en una respuesta HTTP.
    // Se usa en el Service cada vez que necesitas devolver
    // datos de un usuario al cliente.
    //
    // Parámetro:  user — el objeto User que vino de MongoDB
    // Retorna:    UserResponseDTO — sin contraseña, listo para HTTP
    public static UserResponseDTO toResponseDTO(User user) {

        // Verificación defensiva — si por alguna razón llega
        // un user null, lanzamos un error claro en vez de
        // un NullPointerException confuso más adelante.
        if (user == null) {
            return null;
        }

        // Creamos el record UserResponseDTO con todos sus campos.
        // Nota que NO incluimos user.getPassword() — esa es
        // exactamente la protección que buscamos.
        // El orden de los parámetros debe coincidir exactamente
        // con el orden en que los definiste en el record.
        return new UserResponseDTO(
                user.getId(),               // id
                user.getFullName(),         // fullName
                user.getEmail(),            // email
                user.getPhoneNumber(),      // phoneNumber — puede ser null
                user.getShippingAddress(),  // shippingAddress — puede ser null
                user.getCity(),             // city — puede ser null
                user.getCountry(),          // country — puede ser null
                user.getActive(),           // active
                user.getCreatedAt(),        // createdAt — lo puso @CreatedDate
                user.getUpdatedAt()         // updatedAt — lo puso @LastModifiedDate
        );
    }

    // ── MÉTODO 2: CreateUserDTO → User ────────────────────────
    // Convierte los datos que llegaron por HTTP en un objeto
    // User listo para guardar en MongoDB.
    // Se usa en el Service cuando vas a crear un usuario nuevo.
    //
    // IMPORTANTE: este método NO encripta la contraseña.
    // La encriptación es lógica de negocio — vive en el Service.
    // El Mapper solo convierte la estructura, no procesa datos.
    //
    // Parámetro:  dto — los datos que mandó el cliente
    // Retorna:    User — listo para guardar en MongoDB
    public static User toModel(CreateUserDTO dto) {

        if (dto == null) {
            return null;
        }

        // Usamos el Builder de Lombok que definimos en User.java
        // con @Builder. Es más legible que un constructor con
        // 10 parámetros donde podrías confundir el orden.
        return User.builder()
                .fullName(dto.fullName())   // En records los getters no tienen "get"
                .email(dto.email())         // se llaman igual que el campo: dto.email()
                .password(dto.password())   // La contraseña llega aquí en texto plano
                // El Service la encriptará antes de guardar
                .phoneNumber(dto.phoneNumber())
                .shippingAddress(dto.shippingAddress())
                .city(dto.city())
                .country(dto.country())
                // active = true por defecto — definido en User.java con @Builder.Default
                // id = null — MongoDB lo genera automáticamente al guardar
                // createdAt = null — @CreatedDate lo llena automáticamente
                // updatedAt = null — @LastModifiedDate lo llena automáticamente
                .build();
    }

    // ── MÉTODO 3: aplicar UpdateUserDTO sobre un User ─────────
    // Este método es diferente — no crea un User nuevo,
    // sino que actualiza solo los campos que llegaron en el DTO.
    //
    // El truco está en verificar si el campo es null antes
    // de actualizar. Si el cliente mandó solo el teléfono,
    // solo actualizamos el teléfono — el resto queda igual.
    // Eso se llama "partial update" o actualización parcial.
    //
    // Parámetros: user — el usuario existente que vino de MongoDB
    //             dto  — los campos nuevos que mandó el cliente
    // Retorna:    el mismo User con los campos actualizados
    public static User applyUpdates(User user, UpdateUserDTO dto) {

        if (dto == null) {
            return user;
        }

        // Solo actualiza si el cliente mandó ese campo.
        // Si fullName es null significa que el cliente no quiso
        // cambiarlo — lo dejamos como estaba.
        if (dto.fullName() != null && !dto.fullName().isBlank()) {
            user.setFullName(dto.fullName());
        }

        // Para campos opcionales como teléfono solo verificamos
        // que no sea null — pueden ser strings vacíos si el
        // usuario quiere borrar el valor.
        if (dto.phoneNumber() != null) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        if (dto.shippingAddress() != null) {
            user.setShippingAddress(dto.shippingAddress());
        }

        if (dto.city() != null) {
            user.setCity(dto.city());
        }

        if (dto.country() != null) {
            user.setCountry(dto.country());
        }

        // Retornamos el mismo objeto User modificado.
        // Cuando el Service lo guarde con repository.save(user),
        // MongoDB actualizará solo los campos que cambiaron.
        return user;
    }
}
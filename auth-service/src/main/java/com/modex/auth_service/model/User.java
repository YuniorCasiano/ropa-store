// ============================================================
// User.java - Model de usuario en el Auth Service
// Representa un usuario en la base de datos del Auth Service.
//
// IMPORTANTE: Este modelo es diferente al User del User Service.
// El Auth Service solo necesita los campos necesarios para
// autenticar — email, password y si esta activo.
// No necesita direccion, ciudad ni telefono.
//
// Ambos servicios comparten la misma coleccion "users" en
// MongoDB pero cada uno solo usa los campos que necesita.
// ============================================================

package com.modex.auth_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Usamos la misma coleccion "users" que el User Service
// Los dos servicios comparten los mismos documentos en MongoDB
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // ID del documento en MongoDB
    @Id
    private String id;

    // Nombre completo — lo necesitamos para incluirlo
    // en el AuthResponseDTO despues del login
    @Field("full_name")
    private String fullName;

    // Email — el identificador principal del usuario
    // @Indexed(unique = true) garantiza que no haya duplicados
    @Indexed(unique = true)
    private String email;

    // Password encriptada con BCrypt
    // La verificamos en el login con encoder.matches()
    @Field("password")
    private String password;

    // Direccion — opcional, no la usamos en auth
    // pero la incluimos para no perder datos al guardar
    @Field("shipping_address")
    private String shippingAddress;

    // Telefono — opcional
    @Field("phone_number")
    private String phoneNumber;

    // Ciudad — opcional
    @Field("city")
    private String city;

    // Pais — opcional
    @Field("country")
    private String country;

    // Si la cuenta esta activa — solo usuarios activos
    // pueden hacer login
    @Builder.Default
    @Field("active")
    private Boolean active = true;

    // Fechas automaticas
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
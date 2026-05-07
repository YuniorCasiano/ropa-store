// ============================================================
// RefreshToken.java - Model del refresh token
// Representa un token de refresco guardado en MongoDB.
// Cada vez que un usuario hace login se crea un documento
// de este tipo en la coleccion "refresh_tokens".
//
// Cuando el usuario cierra sesion, este documento se elimina.
// Cuando el token expira, este documento se elimina.
// Cuando el usuario pide un nuevo access token, este documento
// se busca y verifica antes de generar el nuevo token.
// ============================================================

package com.modex.auth_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

// @Document - marca esta clase como documento de MongoDB.
// Cada refresh token es un documento en la coleccion "refresh_tokens".
@Document(collection = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    // ID generado automaticamente por MongoDB
    @Id
    private String id;

    // El token en si - es una cadena larga y aleatoria unica.
    // @Indexed(unique = true) garantiza que no existan dos
    // documentos con el mismo token en MongoDB.
    // Es como la llave unica de este documento ademas del ID.
    @Indexed(unique = true)
    @Field("token")
    private String token;

    // Email del usuario al que pertenece este token.
    // Usamos el email como identificador porque el User
    // Service es quien tiene los datos completos del usuario.
    // El Auth Service solo necesita saber de quien es el token.
    @Field("user_email")
    private String userEmail;

    // Fecha exacta en que expira este token.
    // Usamos Instant en vez de LocalDateTime porque Instant
    // representa un momento exacto en el tiempo universal (UTC).
    // Es mas preciso para calcular expiraciones.
    // Cuando la fecha actual supere este valor, el token expiro.
    @Field("expires_at")
    private Instant expiresAt;

    // Indica si el token fue revocado manualmente.
    // Cuando el usuario cierra sesion ponemos revoked = true.
    // Esto permite invalidar el token antes de que expire.
    // @Builder.Default = valor por defecto cuando usas el Builder
    @Builder.Default
    @Field("revoked")
    private Boolean revoked = false;

    // Fecha de creacion del documento - automatica con @CreatedDate
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
}
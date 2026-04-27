// ============================================================
// User.java — Model del User Service
// Representa un documento de usuario en MongoDB.
// Esta clase define exactamente cómo se ve un usuario
// en la base de datos y qué validaciones debe cumplir.
// ============================================================

package com.ropastore.user_service.model;

// Importaciones de MongoDB — para mapear esta clase a un documento
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

// Importaciones de validación — para validar los datos recibidos
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Importaciones de Lombok — para generar código repetitivo automáticamente
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Importación de Java para manejar fechas
import java.time.LocalDateTime;

// @Document le dice a Spring que esta clase es un documento de MongoDB.
// collection = "users" define el nombre de la colección en la base de datos.
// Es equivalente a @Entity + @Table(name="users") en JPA con PostgreSQL.
@Document(collection = "users")

// @Data genera automáticamente:
// - getters para todos los campos (getName(), getEmail(), etc.)
// - setters para todos los campos (setName(), setEmail(), etc.)
// - equals() y hashCode() para comparar objetos
// - toString() para imprimir el objeto en logs
@Data

// @Builder permite crear objetos así:
// User user = User.builder().name("Juan").email("juan@mail.com").build();
// Más legible que hacer new User() y setear cada campo por separado.
@Builder

// @NoArgsConstructor genera: public User() {}
// MongoDB necesita este constructor vacío para poder
// crear objetos cuando lee documentos de la base de datos.
@NoArgsConstructor

// @AllArgsConstructor genera un constructor con todos los campos.
// Lombok's @Builder lo necesita internamente para funcionar.
@AllArgsConstructor
public class User {

    // @Id marca este campo como el identificador único del documento.
    // MongoDB genera automáticamente un valor único cuando guardas
    // un usuario nuevo — no necesitas generarlo tú.
    // En MongoDB se llama "_id" internamente.
    @Id
    private String id;

    // @NotBlank significa que este campo no puede ser:
    // - null
    // - una cadena vacía ""
    // - solo espacios "   "
    // Si llega así, Spring lanza un error de validación automáticamente.
    //
    // @Field("full_name") define cómo se llama este campo en MongoDB.
    // En Java usamos camelCase (fullName) pero en MongoDB guardamos
    // con snake_case (full_name) — es una convención común.
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Field("full_name")
    private String fullName;

    // @Email valida que el valor tenga formato de email válido.
    // Ejemplo válido: juan@gmail.com
    // Ejemplo inválido: juan@, juan, @gmail.com
    //
    // @Indexed(unique = true) crea un índice único en MongoDB.
    // Esto significa que no pueden existir dos usuarios con el
    // mismo email — MongoDB lanza error si lo intentas.
    // Es como un UNIQUE constraint en SQL.
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Indexed(unique = true)
    private String email;

    // La contraseña se guarda encriptada — nunca en texto plano.
    // El Auth Service se encargará de encriptarla con BCrypt
    // antes de guardarla aquí.
    // Por eso no tiene @Email ni validaciones especiales de formato —
    // cuando llegue aquí ya será un hash como:
    // "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjTYcrdlt.y"
    @NotBlank(message = "La contraseña es obligatoria")
    @Field("password")
    private String password;

    // Número de teléfono — opcional, por eso no tiene @NotBlank.
    // Un usuario puede registrarse sin teléfono.
    @Field("phone_number")
    private String phoneNumber;

    // Dirección de envío donde llegará la ropa comprada.
    // También opcional al registrarse — puede agregarse después.
    @Field("shipping_address")
    private String shippingAddress;

    // Ciudad del usuario — útil para calcular costos de envío.
    @Field("city")
    private String city;

    // País del usuario.
    @Field("country")
    private String country;

    // Indica si la cuenta está activa o fue desactivada.
    // Usamos esto en vez de eliminar usuarios — si un usuario
    // "elimina" su cuenta, solo ponemos active = false.
    // Así conservamos el historial de pedidos.
    // @Builder.Default le dice a Lombok que cuando uses el Builder,
    // este campo empiece con el valor true por defecto.
    @Builder.Default
    @Field("active")
    private Boolean active = true;

    // @CreatedDate hace que Spring guarde automáticamente la fecha
    // y hora exacta en que se creó este documento en MongoDB.
    // No necesitas setearla manualmente — Spring lo hace solo.
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    // @LastModifiedDate guarda automáticamente la fecha y hora
    // de la última modificación del documento.
    // Si actualizas el nombre del usuario, este campo se actualiza solo.
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
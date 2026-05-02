// ============================================================
// UserRepository.java — Repositorio de usuarios
// Interface que maneja todas las operaciones de base de datos
// relacionadas con usuarios en MongoDB.
//
// IMPORTANTE: Esta es una INTERFACE, no una clase.
// Tú solo declaras los métodos — Spring genera el código
// que va a MongoDB automáticamente al arrancar.
//
// Al extender MongoRepository heredas gratis:
// save(), findById(), findAll(), deleteById(),
// existsById(), count() y varios más.
// ============================================================

package com.ropastore.user_service.repository;

// Importamos el Model User — el Repository trabaja con Users
import com.ropastore.user_service.model.User;

// MongoRepository es la interface base de Spring Data MongoDB.
// Recibe dos parámetros:
// - User: el tipo de documento que maneja este repository
// - String: el tipo del campo @Id en el Model (nuestro id es String)
import org.springframework.data.mongodb.repository.MongoRepository;

// @Repository le dice a Spring que esta interface es un componente
// de acceso a datos. Spring la implementa automáticamente.
import org.springframework.stereotype.Repository;

// Optional permite manejar resultados que pueden no existir
// sin riesgo de NullPointerException.
import java.util.Optional;

// @Repository — marca esta interface como componente de Spring.
// Spring la detecta con @ComponentScan y la implementa sola.
// No necesitas escribir ninguna implementación.
@Repository

// Extendemos MongoRepository<User, String>:
// - User   = el tipo de documento que maneja
// - String = el tipo del @Id en User.java (nuestro id es String)
// Al extender esta interface heredamos automáticamente:
// save(), findById(), findAll(), deleteById(), count(), etc.
public interface UserRepository extends MongoRepository<User, String> {

    // ── QUERY DERIVADO 1 ─────────────────────────────────────
    // Spring lee "findByEmail" y genera automáticamente:
    // db.users.findOne({ email: email })
    //
    // Devuelve Optional<User> porque el usuario puede no existir.
    // Si existe → Optional con el User adentro.
    // Si no existe → Optional vacío (nunca null).
    //
    // Lo usamos en el Service para:
    // 1. Verificar si el email ya está registrado antes de crear
    // 2. Buscar un usuario por su email para el login
    Optional<User> findByEmail(String email);

    // ── QUERY DERIVADO 2 ─────────────────────────────────────
    // Spring genera: db.users.find({ active: true })
    //
    // Devuelve una lista de todos los usuarios activos.
    // Usamos esto en vez de findAll() para no mostrar usuarios
    // que han desactivado su cuenta.
    // Una lista vacía es válida — no lanza error si no hay usuarios.
    java.util.List<User> findByActiveTrue();

    // ── QUERY DERIVADO 3 ─────────────────────────────────────
    // Spring genera una verificación de existencia:
    // db.users.count({ email: email }) > 0
    //
    // Devuelve true si existe un usuario con ese email.
    // Devuelve false si no existe.
    //
    // Lo usamos en el Service para verificar duplicados
    // ANTES de intentar guardar un usuario nuevo.
    // Es más eficiente que findByEmail() porque no trae
    // todo el documento — solo verifica si existe.
    boolean existsByEmail(String email);

    // ── QUERY DERIVADO 4 ─────────────────────────────────────
    // Spring genera: db.users.findOne({ email: email, active: true })
    //
    // Busca un usuario activo por email.
    // Lo usamos en el Auth Service para el login —
    // un usuario desactivado no puede iniciar sesión.
    Optional<User> findByEmailAndActiveTrue(String email);
}
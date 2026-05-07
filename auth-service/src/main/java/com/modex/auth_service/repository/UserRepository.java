// ============================================================
// UserRepository.java - Repositorio de usuarios
// del Auth Service.
// Maneja las operaciones de base de datos necesarias
// para autenticar usuarios.
//
// Solo necesitamos buscar y verificar usuarios —
// no creamos ni eliminamos desde aqui excepto en registro.
// ============================================================

package com.modex.auth_service.repository;

import com.modex.auth_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Busca un usuario activo por email.
    // Lo usamos en login para verificar credenciales.
    // Si el usuario no existe o esta inactivo devuelve
    // Optional vacio — el AuthService lanza AuthException.
    Optional<User> findByEmailAndActiveTrue(String email);

    // Verifica si existe un usuario con ese email.
    // Lo usamos en register para evitar duplicados.
    boolean existsByEmail(String email);
}
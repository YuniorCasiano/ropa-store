// ============================================================
// RefreshTokenRepository.java - Repositorio de refresh tokens
// Maneja todas las operaciones de base de datos relacionadas
// con los refresh tokens en MongoDB.
//
// Al extender MongoRepository heredamos gratis:
// save(), findById(), findAll(), deleteById(), count()
//
// Agregamos metodos especificos para refresh tokens
// usando query derivado igual que en el User Service.
// ============================================================

package com.modex.auth_service.repository;

import com.modex.auth_service.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends MongoRepository<RefreshToken, String> {

    // ── QUERY DERIVADO 1 ─────────────────────────────────────
    // Busca un refresh token por su valor.
    // Se usa cuando el cliente manda el refresh token para
    // obtener un nuevo access token.
    // Spring genera: db.refresh_tokens.findOne({ token: token })
    //
    // Devuelve Optional porque el token puede no existir
    // si fue eliminado o nunca existio.
    Optional<RefreshToken> findByToken(String token);

    // ── QUERY DERIVADO 2 ─────────────────────────────────────
    // Busca todos los refresh tokens de un usuario por email.
    // Se usa para verificar cuantas sesiones activas tiene
    // un usuario — util para limitar sesiones simultaneas.
    // Spring genera: db.refresh_tokens.find({ user_email: email })
    java.util.List<RefreshToken> findByUserEmail(String userEmail);

    // ── QUERY DERIVADO 3 ─────────────────────────────────────
    // Elimina todos los refresh tokens de un usuario.
    // Se usa cuando el usuario cierra todas sus sesiones
    // o cuando cambia su password — invalidamos todos
    // sus tokens de una sola vez.
    // Spring genera: db.refresh_tokens.deleteMany({ user_email: email })
    void deleteByUserEmail(String userEmail);

    // ── QUERY DERIVADO 4 ─────────────────────────────────────
    // Verifica si existe un token especifico en MongoDB.
    // Mas eficiente que findByToken() cuando solo necesitas
    // saber si existe sin traer todo el documento.
    // Spring genera: db.refresh_tokens.count({ token: token }) > 0
    boolean existsByToken(String token);
}
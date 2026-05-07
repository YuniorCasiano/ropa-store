// ============================================================
// RefreshTokenService.java - Logica de refresh tokens
// Maneja la creacion, validacion y eliminacion de refresh
// tokens en MongoDB.
//
// El AuthService lo usa para:
// - Crear un refresh token despues del login
// - Verificar un refresh token cuando el cliente lo manda
// - Eliminar un refresh token cuando el usuario cierra sesion
// ============================================================

package com.modex.auth_service.service;

import com.modex.auth_service.exception.AuthException;
import com.modex.auth_service.model.RefreshToken;
import com.modex.auth_service.repository.RefreshTokenRepository;
import com.modex.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // Repositorio para operaciones en MongoDB
    private final RefreshTokenRepository refreshTokenRepository;

    // JwtUtil para obtener la duracion del refresh token
    private final JwtUtil jwtUtil;

    // ── METODO 1: createRefreshToken ─────────────────────────
    // Crea un nuevo refresh token para un usuario y lo guarda
    // en MongoDB.
    //
    // Antes de crear el nuevo token eliminamos todos los tokens
    // anteriores del usuario — asi un usuario solo puede tener
    // una sesion activa a la vez. Si quieres permitir multiples
    // sesiones simplemente elimina esa linea.
    //
    // Parametro: email del usuario para quien se crea el token
    // Retorna: el RefreshToken guardado en MongoDB
    @Transactional
    public RefreshToken createRefreshToken(String userEmail) {

        log.debug("Creando refresh token para: {}", userEmail);

        // Eliminamos todos los tokens anteriores del usuario.
        // Esto garantiza que solo existe una sesion activa.
        // Si el usuario hace login desde otro dispositivo,
        // el token anterior queda invalidado.
        refreshTokenRepository.deleteByUserEmail(userEmail);

        // Calculamos la fecha de expiracion.
        // Instant.now() = momento actual en UTC
        // .plusMillis() = sumamos la duracion del refresh token
        // Resultado: fecha exacta en que expira el token
        Instant expiresAt = Instant.now()
                .plusMillis(jwtUtil.getRefreshExpirationMs());

        // Construimos el RefreshToken con el Builder de Lombok
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtUtil.generateRefreshToken()) // UUID aleatorio
                .userEmail(userEmail)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        // Guardamos en MongoDB y devolvemos el documento guardado
        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token creado para: {}", userEmail);

        return saved;
    }

    // ── METODO 2: verifyRefreshToken ─────────────────────────
    // Verifica que un refresh token sea valido.
    // Un token es valido si:
    // 1. Existe en MongoDB
    // 2. No ha expirado
    // 3. No fue revocado manualmente
    //
    // Si el token expiro lo eliminamos de MongoDB automaticamente
    // para mantener la base de datos limpia.
    //
    // Parametro: el token string que mando el cliente
    // Retorna: el RefreshToken de MongoDB si es valido
    // Lanza: AuthException si el token no es valido
    public RefreshToken verifyRefreshToken(String token) {

        log.debug("Verificando refresh token");

        // Buscamos el token en MongoDB
        // Si no existe lanzamos excepcion
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token no encontrado en MongoDB");
                    return new AuthException("Refresh token invalido o no existe");
                });

        // Verificamos si fue revocado manualmente
        // Esto pasa cuando el usuario cierra sesion explicitamente
        if (refreshToken.getRevoked()) {
            log.warn("Intento de uso de refresh token revocado");
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token fue revocado");
        }

        // Verificamos si expiro.
        // Instant.now().isAfter(expiresAt) = el momento actual
        // es despues de la fecha de expiracion = expiro
        if (Instant.now().isAfter(refreshToken.getExpiresAt())) {
            log.warn("Refresh token expirado para: {}",
                    refreshToken.getUserEmail());
            // Eliminamos el token expirado de MongoDB
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token expirado");
        }

        log.debug("Refresh token valido para: {}",
                refreshToken.getUserEmail());

        return refreshToken;
    }

    // ── METODO 3: revokeRefreshToken ─────────────────────────
    // Revoca un refresh token especifico.
    // Se usa cuando el usuario cierra sesion desde un
    // dispositivo especifico pero quiere mantener otras sesiones.
    //
    // Parametro: el token string a revocar
    @Transactional
    public void revokeRefreshToken(String token) {

        log.debug("Revocando refresh token");

        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Refresh token revocado para: {}",
                            refreshToken.getUserEmail());
                });
    }

    // ── METODO 4: revokeAllUserTokens ────────────────────────
    // Revoca todos los refresh tokens de un usuario.
    // Se usa cuando el usuario cambia su password o cuando
    // quiere cerrar sesion en todos sus dispositivos.
    //
    // Parametro: email del usuario
    @Transactional
    public void revokeAllUserTokens(String userEmail) {

        log.debug("Revocando todos los tokens de: {}", userEmail);

        refreshTokenRepository.deleteByUserEmail(userEmail);

        log.info("Todos los tokens revocados para: {}", userEmail);
    }
}
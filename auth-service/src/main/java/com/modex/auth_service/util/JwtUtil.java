// ============================================================
// JwtUtil.java - Utilidad para generar y validar JWT
// Es la clase mas importante del Auth Service.
// Centraliza toda la logica relacionada con tokens JWT.
//
// Responsabilidades:
// 1. Generar access tokens firmados
// 2. Generar refresh tokens aleatorios
// 3. Extraer el email del usuario de un token
// 4. Verificar si un token es valido
// 5. Verificar si un token expiro
// ============================================================

package com.modex.auth_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// @Slf4j - genera el objeto log automaticamente
@Slf4j

// @Component - marca esta clase como un Bean de Spring.
// No es @Service ni @Repository — es un componente de utilidad
// que Spring administra y puede inyectar donde se necesite.
@Component
public class JwtUtil {

    // @Value inyecta el valor de jwt.secret del application.properties
    // directamente en este campo cuando Spring crea el Bean.
    // El formato es: @Value("${nombre.de.la.propiedad}")
    @Value("${jwt.secret}")
    private String secretString;

    // Duracion del access token en milisegundos
    // Viene de jwt.expiration en application.properties
    // Valor: 3600000 = 1 hora
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // Duracion del refresh token en milisegundos
    // Viene de jwt.refresh-expiration en application.properties
    // Valor: 604800000 = 7 dias
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    // ── METODO PRIVADO: getSecretKey ──────────────────────────
    // Convierte el string secreto en una SecretKey criptografica.
    // SecretKey es el objeto que JJWT necesita para firmar
    // y verificar tokens — no puede usar el string directamente.
    //
    // Keys.hmacShaKeyFor() genera una clave HMAC-SHA a partir
    // de los bytes del string. HMAC-SHA256 es el algoritmo
    // que usamos para firmar los tokens.
    //
    // Es privado porque nadie fuera de esta clase necesita
    // acceder a la clave secreta directamente.
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                secretString.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ── METODO 1: generateAccessToken ────────────────────────
    // Genera un nuevo access token JWT firmado para un usuario.
    //
    // El token contiene:
    // - subject: el email del usuario (identificador principal)
    // - issuedAt: fecha y hora de creacion
    // - expiration: fecha y hora de expiracion (ahora + 1 hora)
    // - firma: generada con la SecretKey usando HS256
    //
    // Parametro: email del usuario para quien se genera el token
    // Retorna: el token JWT como String — ejemplo:
    // "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFuQGdtYWlsLmNvbSJ9.abc"
    public String generateAccessToken(String email) {

        // Date representa fecha y hora en Java.
        // System.currentTimeMillis() devuelve el tiempo actual
        // en milisegundos desde el 1 de enero de 1970 (Unix time).
        Date now = new Date(System.currentTimeMillis());

        // La fecha de expiracion es ahora + jwtExpiration milisegundos
        // Ejemplo: ahora = 10:00, expiracion = 11:00
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);

        // Jwts.builder() construye el token paso a paso
        return Jwts.builder()
                // subject es el identificador principal del token
                // Usamos el email porque es unico por usuario
                .subject(email)
                // issuedAt = cuando se creo el token
                .issuedAt(now)
                // expiration = cuando expira el token
                .expiration(expiration)
                // signWith firma el token con nuestra SecretKey
                // usando el algoritmo HS256
                .signWith(getSecretKey())
                // compact() construye y devuelve el token como String
                .compact();
    }

    // ── METODO 2: generateRefreshToken ───────────────────────
    // Genera un refresh token aleatorio unico.
    //
    // A diferencia del access token, el refresh token NO es un JWT
    // — es simplemente un UUID aleatorio. No contiene informacion
    // del usuario — esa informacion vive en MongoDB junto al token.
    //
    // UUID (Universally Unique Identifier) es un identificador
    // de 128 bits generado aleatoriamente. Es practicamente
    // imposible que dos UUIDs sean iguales.
    // Ejemplo: "550e8400-e29b-41d4-a716-446655440000"
    //
    // Retorna: el UUID como String sin guiones para que sea
    // mas limpio como token
    public String generateRefreshToken() {
        // UUID.randomUUID() genera un UUID aleatorio
        // .toString() lo convierte a String con guiones
        // .replace("-", "") elimina los guiones
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ── METODO 3: extractEmail ────────────────────────────────
    // Extrae el email del usuario del payload del token JWT.
    // El email fue guardado como "subject" cuando generamos
    // el token en generateAccessToken().
    //
    // Parametro: el token JWT como String
    // Retorna: el email del usuario
    public String extractEmail(String token) {
        // parseSignedClaims() verifica la firma y extrae el payload
        // getPayload() devuelve los Claims (pares clave-valor)
        // getSubject() devuelve el subject que pusimos — el email
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ── METODO 4: isTokenValid ────────────────────────────────
    // Verifica si un token JWT es valido.
    // Un token es valido si:
    // 1. La firma es correcta — nadie lo modifico
    // 2. No ha expirado
    // 3. Tiene el formato correcto
    //
    // Parametros:
    // - token: el JWT a verificar
    // - email: el email del usuario que dice ser
    // Retorna: true si el token es valido, false si no
    public boolean isTokenValid(String token, String email) {
        try {
            // Extraemos el email del token
            String tokenEmail = extractEmail(token);

            // El token es valido si:
            // 1. El email del token coincide con el email esperado
            // 2. El token no ha expirado
            return tokenEmail.equals(email) && !isTokenExpired(token);

        } catch (ExpiredJwtException e) {
            // El token expiro — es un caso comun y esperado
            log.debug("Token expirado para email: {}", email);
            return false;
        } catch (MalformedJwtException e) {
            // El token tiene formato incorrecto — posible ataque
            log.warn("Token malformado recibido");
            return false;
        } catch (UnsupportedJwtException e) {
            // El token usa un algoritmo no soportado
            log.warn("Token no soportado recibido");
            return false;
        } catch (Exception e) {
            // Cualquier otro error
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    // ── METODO 5: isTokenExpired ──────────────────────────────
    // Verifica si un token JWT ha expirado.
    // Extrae la fecha de expiracion del token y la compara
    // con la fecha y hora actual.
    //
    // Parametro: el JWT a verificar
    // Retorna: true si expiro, false si aun es valido
    public boolean isTokenExpired(String token) {
        // extractExpiration() extrae la fecha de expiracion
        // .before(new Date()) verifica si esa fecha ya paso
        return extractExpiration(token).before(new Date());
    }

    // ── METODO PRIVADO: extractExpiration ────────────────────
    // Extrae la fecha de expiracion del payload del token.
    // Es privado porque solo lo usa isTokenExpired() internamente.
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    // ── METODO 6: getRefreshExpirationMs ─────────────────────
    // Devuelve la duracion del refresh token en milisegundos.
    // Lo usa RefreshTokenService para calcular la fecha
    // de expiracion del refresh token antes de guardarlo.
    public Long getRefreshExpirationMs() {
        return refreshExpiration;
    }

    // ── METODO 7: getAccessExpirationMs ──────────────────────
    // Devuelve la duracion del access token en milisegundos.
    // Lo usa AuthService para incluir expiresIn en AuthResponseDTO.
    public Long getAccessExpirationMs() {
        return jwtExpiration;
    }
}
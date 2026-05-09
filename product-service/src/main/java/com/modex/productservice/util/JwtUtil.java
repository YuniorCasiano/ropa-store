// ============================================================
// JwtUtil.java - Verificacion de JWT en Product Service
// El Product Service no genera tokens — solo los verifica.
// Copia exacta del JwtUtil del Auth Service porque usa
// la misma clave secreta para verificar la firma.
// ============================================================

package com.modex.productservice.util;

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

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                secretString.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Extrae el email del token JWT
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Verifica si el token es valido para ese email
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token malformado");
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Token no soportado");
            return false;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    // Verifica si el token expiro
    public boolean isTokenExpired(String token) {
        return extractExpiration(token)
                .before(new java.util.Date());
    }

    private java.util.Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
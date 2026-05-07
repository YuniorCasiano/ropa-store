// ============================================================
// AuthService.java - Logica de autenticacion
// Es el cerebro del Auth Service. Coordina el registro,
// login, refresh de tokens y logout.
//
// Usa:
// - UserRepository para buscar usuarios en MongoDB
// - RefreshTokenService para manejar refresh tokens
// - JwtUtil para generar y validar access tokens
// - BCryptPasswordEncoder para verificar passwords
// ============================================================

package com.modex.auth_service.service;

import com.modex.auth_service.dto.AuthResponseDTO;
import com.modex.auth_service.dto.LoginRequestDTO;
import com.modex.auth_service.dto.RegisterRequestDTO;
import com.modex.auth_service.dto.RefreshTokenRequestDTO;
import com.modex.auth_service.exception.AuthException;
import com.modex.auth_service.model.RefreshToken;
import com.modex.auth_service.repository.UserRepository;
import com.modex.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // Repositorio para buscar usuarios en MongoDB
    private final UserRepository userRepository;

    // Servicio para manejar refresh tokens
    private final RefreshTokenService refreshTokenService;

    // Utilidad para generar y validar JWT
    private final JwtUtil jwtUtil;

    // Encriptador de passwords — mismo que en el User Service
    // El numero 12 es el strength de BCrypt
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    // ── METODO 1: register ────────────────────────────────────
    // Registra un usuario nuevo en el sistema.
    // Verifica que el email no exista, encripta la password,
    // guarda el usuario y genera los tokens.
    //
    // Parametro: RegisterRequestDTO con los datos del usuario
    // Retorna: AuthResponseDTO con los tokens generados
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {

        log.debug("Registrando usuario: {}", dto.email());

        // REGLA 1: el email debe ser unico
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Intento de registro con email duplicado: {}",
                    dto.email());
            throw new AuthException(
                    "Ya existe una cuenta con el email: " + dto.email()
            );
        }

        // Encriptamos la password antes de guardar
        String encryptedPassword = passwordEncoder.encode(dto.password());

        // Creamos el usuario usando el Builder
        com.modex.auth_service.model.User user =
                com.modex.auth_service.model.User.builder()
                        .fullName(dto.fullName())
                        .email(dto.email())
                        .password(encryptedPassword)
                        .phoneNumber(dto.phoneNumber())
                        .shippingAddress(dto.shippingAddress())
                        .city(dto.city())
                        .country(dto.country())
                        .active(true)
                        .build();

        // Guardamos en MongoDB
        com.modex.auth_service.model.User savedUser =
                userRepository.save(user);

        log.info("Usuario registrado exitosamente: {}", dto.email());

        // Generamos los tokens para el usuario recien registrado
        return generateTokens(savedUser.getEmail(),
                savedUser.getFullName());
    }

    // ── METODO 2: login ───────────────────────────────────────
    // Autentica un usuario verificando sus credenciales.
    // Busca el usuario por email, verifica la password con
    // BCrypt y genera los tokens si todo es correcto.
    //
    // Parametro: LoginRequestDTO con email y password
    // Retorna: AuthResponseDTO con los tokens generados
    public AuthResponseDTO login(LoginRequestDTO dto) {

        log.debug("Intento de login para: {}", dto.email());

        // Buscamos el usuario activo por email
        // Si no existe lanzamos AuthException — HTTP 401
        com.modex.auth_service.model.User user =
                userRepository.findByEmailAndActiveTrue(dto.email())
                        .orElseThrow(() -> {
                            log.warn("Login fallido - usuario no encontrado: {}",
                                    dto.email());
                            // Mensaje generico por seguridad — no decimos
                            // si el email no existe o la password es incorrecta
                            // para evitar que alguien enumere usuarios validos
                            return new AuthException(
                                    "Credenciales incorrectas"
                            );
                        });

        // Verificamos la password con BCrypt.
        // encoder.matches(passwordTextoPlano, hashGuardado)
        // Devuelve true si coinciden, false si no.
        boolean passwordCorrecta = passwordEncoder.matches(
                dto.password(),
                user.getPassword()
        );

        if (!passwordCorrecta) {
            log.warn("Login fallido - password incorrecta para: {}",
                    dto.email());
            // Mismo mensaje generico por seguridad
            throw new AuthException("Credenciales incorrectas");
        }

        log.info("Login exitoso para: {}", dto.email());

        // Generamos y devolvemos los tokens
        return generateTokens(user.getEmail(), user.getFullName());
    }

    // ── METODO 3: refreshToken ────────────────────────────────
    // Genera un nuevo access token usando el refresh token.
    // El cliente llama a este endpoint cuando su access token
    // expiro y necesita uno nuevo sin hacer login de nuevo.
    //
    // Parametro: RefreshTokenRequestDTO con el refresh token
    // Retorna: AuthResponseDTO con el nuevo access token
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO dto) {

        log.debug("Solicitud de refresh token");

        // Verificamos que el refresh token sea valido en MongoDB
        // Si no es valido RefreshTokenService lanza AuthException
        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(dto.refreshToken());

        // Buscamos el usuario dueno del token
        com.modex.auth_service.model.User user =
                userRepository.findByEmailAndActiveTrue(
                                refreshToken.getUserEmail())
                        .orElseThrow(() -> new AuthException(
                                "Usuario no encontrado o inactivo"
                        ));

        // Generamos solo un nuevo access token
        // El refresh token sigue siendo el mismo
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail()
        );

        log.info("Access token renovado para: {}", user.getEmail());

        // Devolvemos el nuevo access token con el mismo refresh token
        return new AuthResponseDTO(
                newAccessToken,
                dto.refreshToken(),  // mismo refresh token
                "Bearer",
                jwtUtil.getAccessExpirationMs(),
                user.getEmail(),
                user.getFullName()
        );
    }

    // ── METODO 4: logout ──────────────────────────────────────
    // Cierra la sesion del usuario revocando su refresh token.
    // El access token expirara solo cuando llegue su tiempo —
    // no podemos invalidarlo porque es stateless.
    // Por eso los access tokens tienen duracion corta (1 hora).
    //
    // Parametro: el refresh token a revocar
    @Transactional
    public void logout(String refreshToken) {

        log.debug("Cerrando sesion");

        refreshTokenService.revokeRefreshToken(refreshToken);

        log.info("Sesion cerrada exitosamente");
    }

    // ── METODO PRIVADO: generateTokens ────────────────────────
    // Metodo auxiliar que genera access token y refresh token
    // para un usuario. Lo usan register() y login() para
    // evitar repetir el mismo codigo en ambos metodos.
    // Eso se llama principio DRY — Don't Repeat Yourself.
    //
    // Parametros: email y fullName del usuario
    // Retorna: AuthResponseDTO con ambos tokens
    private AuthResponseDTO generateTokens(String email,
                                           String fullName) {

        // Generamos el access token JWT
        String accessToken = jwtUtil.generateAccessToken(email);

        // Creamos y guardamos el refresh token en MongoDB
        RefreshToken refreshToken = refreshTokenService
                .createRefreshToken(email);

        // Construimos la respuesta con el factory method
        return AuthResponseDTO.of(
                accessToken,
                refreshToken.getToken(),
                jwtUtil.getAccessExpirationMs(),
                email,
                fullName
        );
    }
}

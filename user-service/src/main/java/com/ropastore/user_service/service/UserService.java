// ============================================================
// UserService.java — Lógica de negocio del User Service
// Es el cerebro del microservicio. Toma decisiones, aplica
// reglas de negocio y coordina el Repository y el Mapper.
//
// El Controller le pasa datos → el Service decide qué hacer
// → el Repository ejecuta en MongoDB → el Mapper convierte
// el resultado → el Service devuelve el DTO al Controller.
// ============================================================

package com.ropastore.user_service.service;

// Importaciones de nuestras propias clases
import com.ropastore.user_service.dto.CreateUserDTO;
import com.ropastore.user_service.dto.UpdateUserDTO;
import com.ropastore.user_service.dto.UserResponseDTO;
import com.ropastore.user_service.exception.UserNotFoundException;
import com.ropastore.user_service.mapper.UserMapper;
import com.ropastore.user_service.model.User;
import com.ropastore.user_service.repository.UserRepository;

// Importaciones de Spring
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Importaciones de Java
import java.util.List;
import java.util.stream.Collectors;

// @Slf4j — Lombok genera automáticamente el objeto "log" para
// registrar mensajes en la consola. Se usa así:
// log.info("mensaje") → información normal
// log.error("mensaje") → errores
// log.debug("mensaje") → detalles de depuración
@Slf4j

// @Service — marca esta clase como un Bean de servicio.
// Spring crea una sola instancia al arrancar y la inyecta
// en el Controller automáticamente.
@Service

// @RequiredArgsConstructor — Lombok genera el constructor con
// todos los campos "final". Spring usa ese constructor para
// inyectar las dependencias automáticamente.
// Es equivalente a poner @Autowired en cada campo pero
// más limpio y recomendado en proyectos modernos.
@RequiredArgsConstructor
public class UserService {

    // final = este campo nunca cambia después de ser inyectado.
    // Spring inyecta el UserRepository automáticamente
    // gracias a @RequiredArgsConstructor.
    private final UserRepository userRepository;

    // BCryptPasswordEncoder es la clase que encripta contraseñas.
    // El número 12 es el "strength" — cuántas veces aplica el
    // algoritmo. A mayor número más seguro pero más lento.
    // 10-12 es el estándar recomendado para producción.
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    // ── MÉTODO 1: Crear usuario ───────────────────────────────
    // Recibe un CreateUserDTO del Controller, valida las reglas
    // de negocio, encripta la contraseña y guarda en MongoDB.
    //
    // @Transactional — si algo falla durante el proceso,
    // todos los cambios se revierten automáticamente.
    @Transactional
    public UserResponseDTO createUser(CreateUserDTO dto) {

        // REGLA DE NEGOCIO 1: el email debe ser único.
        // Verificamos ANTES de intentar guardar para dar un
        // error claro en vez de un error técnico de MongoDB.
        log.debug("Verificando si el email ya existe: {}", dto.email());

        if (userRepository.existsByEmail(dto.email())) {
            // Lanzamos una excepción con un mensaje claro.
            // El GlobalExceptionHandler la capturará y devolverá
            // una respuesta HTTP 409 (Conflict) al cliente.
            log.warn("Intento de registro con email duplicado: {}", dto.email());
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el email: " + dto.email()
            );
        }

        // Convertimos el DTO a Model usando el Mapper.
        // En este punto la contraseña todavía está en texto plano.
        User user = UserMapper.toModel(dto);

        // REGLA DE NEGOCIO 2: nunca guardar contraseñas en texto plano.
        // Encriptamos con BCrypt antes de guardar en MongoDB.
        // El resultado es un hash irreversible como:
        // "$2a$12$N9qo8uLOickgx2ZMRZoMye..."
        String encryptedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(encryptedPassword);

        // Guardamos el usuario en MongoDB.
        // MongoDB genera el ID automáticamente.
        // @CreatedDate y @LastModifiedDate se llenan solos
        // gracias a MongoConfig.java.
        User savedUser = userRepository.save(user);

        log.info("Usuario creado exitosamente con id: {}", savedUser.getId());

        // Convertimos el Model guardado a DTO de respuesta.
        // UserMapper.toResponseDTO NUNCA incluye la contraseña.
        return UserMapper.toResponseDTO(savedUser);
    }

    // ── MÉTODO 2: Buscar usuario por ID ──────────────────────
    // Busca un usuario en MongoDB por su ID.
    // Si no existe lanza UserNotFoundException que el
    // GlobalExceptionHandler convierte en HTTP 404.
    public UserResponseDTO getUserById(String id) {

        log.debug("Buscando usuario por id: {}", id);

        // findById devuelve Optional<User>.
        // orElseThrow() extrae el User si existe,
        // o lanza la excepción si el Optional está vacío.
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con id: {}", id);
                    return new UserNotFoundException(id);
                });

        log.debug("Usuario encontrado: {}", user.getEmail());

        // Convertimos a DTO antes de devolver — nunca devolvemos
        // el Model directamente al Controller.
        return UserMapper.toResponseDTO(user);
    }

    // ── MÉTODO 3: Buscar usuario por email ───────────────────
    // Busca un usuario por su email.
    // Lo usará el Auth Service para verificar credenciales
    // durante el login.
    public UserResponseDTO getUserByEmail(String email) {

        log.debug("Buscando usuario por email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con email: {}", email);
                    return new UserNotFoundException(
                            "No existe usuario con el email: " + email
                    );
                });

        return UserMapper.toResponseDTO(user);
    }

    // ── MÉTODO 4: Listar todos los usuarios activos ───────────
    // Devuelve la lista de todos los usuarios con cuenta activa.
    // Usa findByActiveTrue() del Repository para excluir
    // automáticamente los usuarios desactivados.
    public List<UserResponseDTO> getAllUsers() {

        log.debug("Obteniendo lista de todos los usuarios activos");

        // findByActiveTrue() devuelve List<User>.
        // .stream() convierte la lista en un flujo de datos
        //   que podemos transformar.
        // .map(UserMapper::toResponseDTO) convierte cada User
        //   en UserResponseDTO usando el Mapper.
        // .collect(Collectors.toList()) junta los resultados
        //   de vuelta en una List<UserResponseDTO>.
        return userRepository.findByActiveTrue()
                .stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── MÉTODO 5: Actualizar usuario ──────────────────────────
    // Actualiza solo los campos que el cliente mandó.
    // Si un campo es null en el DTO significa que el cliente
    // no quiso cambiarlo — el Mapper lo maneja con applyUpdates().
    @Transactional
    public UserResponseDTO updateUser(String id, UpdateUserDTO dto) {

        log.debug("Actualizando usuario con id: {}", id);

        // Primero verificamos que el usuario existe.
        // Si no existe lanzamos UserNotFoundException (HTTP 404).
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // El Mapper aplica solo los campos que no son null en el DTO.
        // Si el cliente solo mandó el teléfono, solo se actualiza
        // el teléfono — el nombre, ciudad y país quedan igual.
        UserMapper.applyUpdates(user, dto);

        // Guardamos los cambios en MongoDB.
        // @LastModifiedDate se actualiza automáticamente.
        User updatedUser = userRepository.save(user);

        log.info("Usuario actualizado exitosamente: {}", id);

        return UserMapper.toResponseDTO(updatedUser);
    }

    // ── MÉTODO 6: Desactivar usuario ──────────────────────────
    // "Elimina" un usuario poniendo active = false.
    // NO borramos el documento de MongoDB porque necesitamos
    // conservar el historial de pedidos asociado al usuario.
    // Esto se llama "soft delete" — borrado suave.
    @Transactional
    public void deactivateUser(String id) {

        log.debug("Desactivando usuario con id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Solo cambiamos el flag active a false.
        // El documento sigue existiendo en MongoDB.
        user.setActive(false);
        userRepository.save(user);

        log.info("Usuario desactivado: {}", id);
    }
}
// ============================================================
// UserController.java — Endpoints HTTP del User Service
// Define las URLs de la API y recibe las peticiones HTTP.
// No tiene lógica de negocio — solo recibe, valida y delega
// al UserService.
//
// Endpoints disponibles:
// POST   /api/users          → crear usuario
// GET    /api/users          → listar todos los usuarios activos
// GET    /api/users/{id}     → obtener usuario por ID
// PUT    /api/users/{id}     → actualizar usuario
// DELETE /api/users/{id}     → desactivar usuario
// ============================================================

package com.ropastore.user_service.controller;

// Importaciones de nuestras clases
import com.ropastore.user_service.dto.CreateUserDTO;
import com.ropastore.user_service.dto.UpdateUserDTO;
import com.ropastore.user_service.dto.UserResponseDTO;
import com.ropastore.user_service.service.UserService;

// Importaciones de Spring
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Importaciones de Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Importaciones de Java
import java.util.List;

// @Slf4j — genera el objeto log para registrar peticiones
@Slf4j

// @RestController — marca esta clase como Controller REST.
// Combina @Controller y @ResponseBody — todos los métodos
// devuelven JSON automáticamente sin necesidad de anotaciones
// adicionales en cada método.
@RestController

// @RequestMapping — define la URL base de todos los endpoints.
// Todos los métodos de esta clase tendrán URLs que empiezan
// con /api/users. Esto se llama "prefijo de ruta".
@RequestMapping("/api/users")

// @RequiredArgsConstructor — Lombok inyecta el UserService
// automáticamente a través del constructor.
@RequiredArgsConstructor
public class UserController {

    // Spring inyecta el UserService automáticamente.
    // El Controller delega TODA la lógica al Service.
    private final UserService userService;

    // ── ENDPOINT 1: Crear usuario ─────────────────────────────
    // POST /api/users
    // Recibe los datos del nuevo usuario en el body como JSON.
    // Devuelve el usuario creado con HTTP 201 Created.
    //
    // Ejemplo de request body:
    // {
    //   "fullName": "Juan Pérez",
    //   "email": "juan@gmail.com",
    //   "password": "miPassword123",
    //   "phoneNumber": "8091234567",
    //   "city": "Santo Domingo",
    //   "country": "República Dominicana"
    // }
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(

            // @Valid — activa las validaciones del DTO.
            // Si algún campo falla @NotBlank, @Email o @Size,
            // Spring lanza MethodArgumentNotValidException
            // antes de llegar aquí — el GlobalExceptionHandler
            // la captura y devuelve HTTP 400 con los errores.
            //
            // @RequestBody — indica que el JSON del body de la
            // petición se convierte automáticamente a CreateUserDTO.
            @Valid @RequestBody CreateUserDTO dto) {

        log.info("POST /api/users - Creando usuario con email: {}", dto.email());

        // Delegamos al Service — el Controller no decide nada.
        UserResponseDTO createdUser = userService.createUser(dto);

        // HTTP 201 Created — código estándar cuando se crea
        // un recurso nuevo exitosamente.
        // ResponseEntity.status(CREATED).body(createdUser) construye
        // la respuesta con código 201 y el JSON del usuario creado.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    // ── ENDPOINT 2: Listar todos los usuarios ─────────────────
    // GET /api/users
    // No recibe parámetros — devuelve todos los usuarios activos.
    // Devuelve HTTP 200 OK con la lista en JSON.
    //
    // Ejemplo de respuesta:
    // [
    //   { "id": "abc123", "fullName": "Juan", "email": "j@m.com" },
    //   { "id": "def456", "fullName": "María", "email": "m@m.com" }
    // ]
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        log.info("GET /api/users - Obteniendo todos los usuarios activos");

        List<UserResponseDTO> users = userService.getAllUsers();

        // ResponseEntity.ok() es un shortcut para
        // ResponseEntity.status(200).body(users)
        return ResponseEntity.ok(users);
    }

    // ── ENDPOINT 3: Obtener usuario por ID ────────────────────
    // GET /api/users/{id}
    // El {id} en la URL es una variable — Spring la extrae
    // automáticamente y la pasa al método como parámetro.
    //
    // Ejemplo: GET /api/users/64f1a2b3c4d5e6f7a8b9c0d1
    // Devuelve HTTP 200 con el usuario o HTTP 404 si no existe.
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(

            // @PathVariable extrae el valor {id} de la URL
            // y lo asigna al parámetro String id.
            @PathVariable String id) {

        log.info("GET /api/users/{} - Buscando usuario por id", id);

        UserResponseDTO user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    // ── ENDPOINT 4: Actualizar usuario ────────────────────────
    // PUT /api/users/{id}
    // Actualiza solo los campos que lleguen en el body.
    // Si un campo no viene en el JSON no se modifica.
    // Devuelve HTTP 200 con el usuario actualizado.
    //
    // Ejemplo de request body — solo actualiza el teléfono:
    // { "phoneNumber": "8097654321" }
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(

            // ID del usuario a actualizar — viene en la URL
            @PathVariable String id,

            // Datos a actualizar — vienen en el body
            // @Valid activa las validaciones del UpdateUserDTO
            @Valid @RequestBody UpdateUserDTO dto) {

        log.info("PUT /api/users/{} - Actualizando usuario", id);

        UserResponseDTO updatedUser = userService.updateUser(id, dto);

        return ResponseEntity.ok(updatedUser);
    }

    // ── ENDPOINT 5: Desactivar usuario ────────────────────────
    // DELETE /api/users/{id}
    // No elimina el documento de MongoDB — solo pone active=false.
    // Esto se llama "soft delete" — conservamos el historial
    // de pedidos del usuario.
    // Devuelve HTTP 204 No Content — éxito sin cuerpo de respuesta.
    // HTTP 204 es el estándar para operaciones DELETE exitosas.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(

            @PathVariable String id) {

        log.info("DELETE /api/users/{} - Desactivando usuario", id);

        userService.deactivateUser(id);

        // HTTP 204 No Content — operación exitosa sin cuerpo.
        // ResponseEntity.noContent().build() crea una respuesta
        // 204 sin body — estándar para DELETE exitoso.
        return ResponseEntity.noContent().build();
    }
}
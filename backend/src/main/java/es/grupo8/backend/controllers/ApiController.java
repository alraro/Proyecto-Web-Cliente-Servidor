package es.grupo8.backend.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final JdbcTemplate jdbcTemplate;
    private final JwtService jwtService;

    public ApiController(JdbcTemplate jdbcTemplate, JwtService jwtService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
    }

    @GetMapping("/ejemplo")
    public List<String> obtenerEstado() {
        return List.of("Backend funcionando correctamente", "API de ejemplo");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = trimToNull(request == null ? null : request.email());
        String password = trimToNull(request == null ? null : request.password());

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
        }

        List<LoginResponse> users = jdbcTemplate.query(
                """
                        SELECT id_usuario, nombre, email
                        FROM usuario
                        WHERE email = ? AND contrasena = ?
                        LIMIT 1
                        """,
                (rs, rowNum) -> new LoginResponse(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        "Login correcto",
                        "",
                        "Bearer"
                ),
                email,
                password
        );

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales invalidas"));
        }

        LoginResponse user = users.get(0);
        String token = jwtService.generateToken(user.userId(), user.email(), user.nombre());

        return ResponseEntity.ok(new LoginResponse(
                user.userId(),
                user.nombre(),
                user.email(),
                "Login correcto",
                token,
                "Bearer"
        ));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String nombre = trimToNull(request == null ? null : request.nombre());
        String email = trimToNull(request == null ? null : request.email());
        String password = trimToNull(request == null ? null : request.password());
        String telefono = trimToNull(request == null ? null : request.telefono());
        String domicilio = trimToNull(request == null ? null : request.domicilio());
        String localidad = trimToNull(request == null ? null : request.localidad());
        String cp = trimToNull(request == null ? null : request.cp());

        if (nombre == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nombre, email y contrasena son obligatorios"));
        }

        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "La contrasena debe tener al menos 6 caracteres"));
        }

        Integer existingUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM usuario WHERE email = ?",
                Integer.class,
                email
        );

        if (existingUsers != null && existingUsers > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe un usuario con ese email"));
        }

        Integer userId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO usuario (nombre, email, telefono, contrasena, domicilio, localidad, cp)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        RETURNING id_usuario
                        """,
                Integer.class,
                nombre,
                email,
                telefono,
                password,
                domicilio,
                localidad,
                cp
        );

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo crear el usuario"));
        }

        String token = jwtService.generateToken(userId, email, nombre);

        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(
                userId,
                nombre,
                email,
                "Registro correcto",
                token,
                "Bearer"
        ));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token no proporcionado"));
        }

        String token = authorization.substring(7);

        try {
            Claims claims = jwtService.validateToken(token);
            Integer userId = Integer.parseInt(claims.getSubject());

            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                    """
                            SELECT id_usuario, nombre, email
                            FROM usuario
                            WHERE id_usuario = ?
                            LIMIT 1
                            """,
                    userId
            );

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token invalido"));
            }

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "email", claims.get("email", String.class),
                    "nombre", claims.get("nombre", String.class),
                    "message", "Sesion valida"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token invalido o expirado"));
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

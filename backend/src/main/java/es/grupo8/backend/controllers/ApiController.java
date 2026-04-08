package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.LoginRequest;
import es.grupo8.backend.dto.LoginResponse;
import es.grupo8.backend.services.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        if (request == null || request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
        }

        String email = request.email().trim();
        String password = request.password().trim();

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
        }

        String sql = """
            SELECT id_usuario, nombre, email, contrasena
                FROM usuario
                WHERE email = ? AND contrasena = ?
                LIMIT 1
                """;

        List<LoginResponse> users = jdbcTemplate.query(
                sql,
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

            String sql = """
                SELECT id_usuario, nombre, email
                FROM usuario
                WHERE id_usuario = ?
                LIMIT 1
                """;

            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, userId);
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
}

package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.LoginRequest;
import es.grupo8.backend.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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

    public ApiController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                SELECT id_usuario, nombre, email
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
                        "Login correcto"
                ),
                email,
                password
        );

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales invalidas"));
        }

        return ResponseEntity.ok(users.get(0));
    }
}

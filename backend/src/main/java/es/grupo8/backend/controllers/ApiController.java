package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.LoginRequest;
import es.grupo8.backend.dto.LoginResponse;
import es.grupo8.backend.dto.RegisterRequest;
import es.grupo8.backend.services.CsrfService;
import es.grupo8.backend.services.JwtService;
import es.grupo8.backend.services.PasswordService;
import es.grupo8.backend.services.RateLimitService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final JdbcTemplate jdbcTemplate;
	private final JwtService jwtService;
	private final PasswordService passwordService;
	private final RateLimitService rateLimitService;
	private final CsrfService csrfService;

	public ApiController(
			JdbcTemplate jdbcTemplate,
			JwtService jwtService,
			PasswordService passwordService,
			RateLimitService rateLimitService,
			CsrfService csrfService
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.jwtService = jwtService;
		this.passwordService = passwordService;
		this.rateLimitService = rateLimitService;
		this.csrfService = csrfService;
	}

	@GetMapping({"/ejemplo", "/v1/ejemplo"})
	public List<String> obtenerEstado() {
		return List.of("Backend funcionando correctamente", "API de ejemplo");
	}

	@GetMapping({"/auth/csrf", "/v1/auth/csrf"})
	public ResponseEntity<?> csrfToken() {
		return ResponseEntity.ok(Map.of("csrfToken", csrfService.generateToken()));
	}

	@PostMapping({"/auth/login", "/v1/auth/login"})
	public ResponseEntity<?> login(
			@RequestBody LoginRequest request,
			@RequestHeader(value = "X-CSRF-Token", required = false) String csrfHeader,
			HttpServletRequest httpRequest
	) {
		String email = trimToNull(request == null ? null : request.email());
		String password = trimToNull(request == null ? null : request.password());
		String csrfToken = trimToNull(request == null ? null : request.csrfToken());

		if (csrfToken == null || !csrfToken.equals(csrfHeader) || !csrfService.validateAndConsume(csrfToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Token CSRF invalido"));
		}

		if (email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
		}

		String rateLimitKey = buildRateLimitKey(email, httpRequest);
		if (rateLimitService.isBlocked(rateLimitKey)) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.header("Retry-After", String.valueOf(rateLimitService.retryAfterSeconds(rateLimitKey)))
					.body(Map.of("message", "Demasiados intentos. Intenta de nuevo en unos minutos"));
		}

		List<Map<String, Object>> users = jdbcTemplate.queryForList(
				"""
						SELECT id_usuario, nombre, email, contrasena
						FROM usuario
						WHERE email = ?
						LIMIT 1
						""",
				email
		);

		if (users.isEmpty()) {
			rateLimitService.registerFailure(rateLimitKey);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		Map<String, Object> user = users.get(0);
		Integer userId = ((Number) user.get("id_usuario")).intValue();
		String nombre = String.valueOf(user.get("nombre"));
		String userEmail = String.valueOf(user.get("email"));
		String storedPassword = String.valueOf(user.get("contrasena"));

		if (!passwordService.matches(password, storedPassword)) {
			rateLimitService.registerFailure(rateLimitKey);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		if (passwordService.needsMigration(storedPassword)) {
			jdbcTemplate.update(
					"UPDATE usuario SET contrasena = ? WHERE id_usuario = ?",
					passwordService.hash(password),
					userId
			);
		}

		rateLimitService.registerSuccess(rateLimitKey);
		String token = jwtService.generateToken(userId, userEmail, nombre);

		return ResponseEntity.ok(new LoginResponse(
				userId,
				nombre,
				userEmail,
				"Login correcto",
				token,
				"Bearer",
				jwtService.getExpirationSeconds()
		));
	}

	@PostMapping({"/auth/register", "/v1/auth/register"})
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
				passwordService.hash(password),
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
				"Bearer",
				jwtService.getExpirationSeconds()
		));
	}

	@GetMapping({"/auth/me", "/v1/auth/me"})
	public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token no proporcionado"));
		}

		String token = authorization.substring(7);

		try {
			Claims claims = jwtService.validateToken(token);
			int userId = Integer.parseInt(claims.getSubject());

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
		} catch (RuntimeException ex) {
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

	private static String buildRateLimitKey(String email, HttpServletRequest request) {
		String clientIp = request.getHeader("X-Forwarded-For");
		if (clientIp != null && clientIp.contains(",")) {
			clientIp = clientIp.split(",")[0].trim();
		}
		if (clientIp == null || clientIp.isBlank()) {
			clientIp = request.getRemoteAddr();
		}

		return email.toLowerCase() + "@" + clientIp;
	}
}

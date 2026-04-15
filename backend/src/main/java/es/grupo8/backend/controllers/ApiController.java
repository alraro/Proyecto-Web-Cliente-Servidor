package es.grupo8.backend.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Controller
public class ApiController {

	@Autowired
	protected UserRepository userRepository;

	@Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms:7200000}")
	private long jwtExpirationMs;

	private SecretKey signingKey;

	@PostConstruct
	public void initJwt() {
		signingKey = buildSigningKey(jwtSecret);
	}

	@GetMapping({"/", "/index"})
	public String doInit(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Inicio");
		return "index";
	}

	@GetMapping("/login")
	public String doLogin(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
		return "login";
	}

	@GetMapping("/register")
	public String doRegister(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
		return "register";
	}

	@GetMapping("/password")
	public String doPassword(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Recuperar contraseña");
		return "password";
	}

	@GetMapping("/api/ejemplo")
	@ResponseBody
	public List<String> obtenerEstado() {
		return List.of("Backend funcionando correctamente", "API de ejemplo");
	}

	@PostMapping("/api/auth/password-recovery")
	@ResponseBody
	public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> request) {
		String email = normalizeEmail(request == null ? null : request.get("email"));

		if (email == null || !isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Introduce un correo valido"));
		}

		userRepository.findByEmail(email);

		return ResponseEntity.ok(Map.of(
				"message", "Si el correo existe, recibirás instrucciones para restablecer la contraseña"
		));
	}

	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));

		if (email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
		}

		UserEntity user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		String storedPassword = user.getContrasena();

		if (!matchesPassword(password, storedPassword)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		if (needsMigration(storedPassword)) {
			user.setContrasena(hashPassword(password));
			userRepository.save(user);
		}

		String token = generateToken(user.getIdUsuario(), user.getEmail(), user.getNombre());

		return ResponseEntity.ok(Map.of(
				"userId", user.getIdUsuario(),
				"nombre", user.getNombre(),
				"email", user.getEmail(),
				"message", "Login correcto",
				"token", token,
				"tokenType", "Bearer",
				"expiresInSeconds", Math.max(1, jwtExpirationMs / 1000)
		));
	}

	@PostMapping("/api/auth/register")
	@ResponseBody
	public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
		String nombre = trimToNull(request == null ? null : request.get("nombre"));
		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));
		String telefono = trimToNull(request == null ? null : request.get("telefono"));
		String domicilio = trimToNull(request == null ? null : request.get("domicilio"));
		String localidad = trimToNull(request == null ? null : request.get("localidad"));
		String cp = trimToNull(request == null ? null : request.get("cp"));

		if (nombre == null || email == null || password == null || telefono == null || domicilio == null || localidad == null || cp == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Nombre, email, telefono, contrasena, domicilio, localidad y CP son obligatorios"));
		}

		if (!isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email no tiene un formato valido"));
		}

		if (containsXss(nombre) || containsXss(domicilio) || containsXss(localidad)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Se detectaron caracteres no permitidos"));
		}

		if (password.length() < 6) {
			return ResponseEntity.badRequest().body(Map.of("message", "La contrasena debe tener al menos 6 caracteres"));
		}

		if (userRepository.existsByEmail(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Ya existe un usuario con ese email"));
		}

		UserEntity user = new UserEntity();
		user.setNombre(nombre);
		user.setEmail(email);
		user.setTelefono(telefono);
		user.setContrasena(hashPassword(password));
		user.setDomicilio(domicilio);
		user.setCp(cp);

		UserEntity createdUser = userRepository.save(user);
		String token = generateToken(createdUser.getIdUsuario(), createdUser.getEmail(), createdUser.getNombre());

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"userId", createdUser.getIdUsuario(),
				"nombre", createdUser.getNombre(),
				"email", createdUser.getEmail(),
				"message", "Registro correcto",
				"token", token,
				"tokenType", "Bearer",
				"expiresInSeconds", Math.max(1, jwtExpirationMs / 1000)
		));
	}

	@PostMapping("/anadir")
	public String doAnadir(Model model) {
		return this.doRegister(model);
	}

	@GetMapping("/editar")
	public String doEditar(@RequestParam(value = "id", required = false) Integer id, Model model) {
		model.addAttribute("selectedId", id);
		return this.doRegister(model);
	}

	@PostMapping("/guardar")
	public String doGuardar() {
		return "redirect:/";
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String normalizeEmail(String email) {
		String trimmed = trimToNull(email);
		return trimmed == null ? null : trimmed.toLowerCase();
	}

	private static boolean isValidEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	private static boolean containsXss(String value) {
		String lowered = value.toLowerCase();
		return lowered.contains("<") || lowered.contains(">") || lowered.contains("script");
	}

	private static String hashPassword(String rawPassword) {
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
	}

	private static boolean matchesPassword(String rawPassword, String storedPassword) {
		if (rawPassword == null || storedPassword == null) {
			return false;
		}

		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
			return BCrypt.checkpw(rawPassword, storedPassword);
		}

		return rawPassword.equals(storedPassword);
	}

	private static boolean needsMigration(String storedPassword) {
		return !(storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
	}

	private String generateToken(Integer userId, String email, String nombre) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("nombre", nombre)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(jwtExpirationMs)))
				.signWith(signingKey)
				.compact();
	}

	private static SecretKey buildSigningKey(String configuredSecret) {
		try {
			byte[] decoded = Decoders.BASE64.decode(configuredSecret);
			return Keys.hmacShaKeyFor(decoded);
		} catch (IllegalArgumentException ignored) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(configuredSecret.getBytes(StandardCharsets.UTF_8));
				return Keys.hmacShaKeyFor(hash);
			} catch (NoSuchAlgorithmException ex) {
				throw new IllegalStateException("No se pudo inicializar la clave JWT", ex);
			}
		}
	}
}

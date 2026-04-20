package es.grupo8.backend.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Controller
public class ApiController {

	@Autowired
	protected UserRepository userRepository;

	// Usa JWT para autenticación en lugar de sesiones tradicionales, lo que es más adecuado para APIs RESTful y aplicaciones modernas.
	// Hace que el usuario mantenga la sesión activa tras loguearse, incluso después de cerrar el navegador, hasta que el token expire o se revoque.
	@Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms:7200000}")
	private long jwtExpirationMs;

	private SecretKey signingKey;

	@PostConstruct
	public void initJwt() {
		signingKey = buildSigningKey(jwtSecret);
	}




	// Pagina de inicio
	@GetMapping({"/", "/index"})
	public String doInit(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Inicio");
		return "index";
	}

	// Página de login
	@GetMapping("/login")
	public String doLogin(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
		return "login";
	}

	// Página de registro
	@GetMapping("/register")
	public String doRegister(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
		return "register";
	}

	// Endpoint para login
	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

		// Sacamos email y contraseña
		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));

		// Validamos el email y contraseña
		if (email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email y contrasena son obligatorios"));
		}

		// Buscamos al usuario por email
		UserEntity user = userRepository.findByEmail(email).orElse(null);
		
		// Si no existe el usuario, fuera
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}


		String storedPassword = user.getContrasena();
		// Verificamos la contraseña proporcionada con la almacenada
		if (!matchesPassword(password, storedPassword)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		// Si no está en hash la cambiamos
		if (needsMigration(storedPassword)) {
			user.setContrasena(hashPassword(password));
			userRepository.save(user);
		}

		// Generamos token JWT con ID usuario, email y nombre
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



	// Endpoint para registro
	@PostMapping("/api/auth/register")
	@ResponseBody
	public ResponseEntity<?> register(@RequestBody Map<String, String> request) {

		String nombre = trimToNull(request == null ? null : request.get("nombre"));
		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));
		String telefono = trimToNull(request == null ? null : request.get("telefono"));
		String domicilio = trimToNull(request == null ? null : request.get("domicilio"));
		String cp = trimToNull(request == null ? null : request.get("cp"));

		// Comprobamos datos obligatorios
		if (nombre == null || email == null || password == null || telefono == null || domicilio == null || cp == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Nombre, email, telefono, contrasena, domicilio y CP son obligatorios"));
		}

		// Validamos formato de email
		if (!isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email no tiene un formato valido"));
		}

		// Validamos formato telefono
		if (!isValidPhone(telefono)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El telefono no tiene un formato valido"));
		}

		// Validamos formato código postal
		if (!isValidPostalCode(cp)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El codigo postal no es valido"));
		}

		// Validamos tamaño minimo contraseña
		if (password.length() < 6) {
			return ResponseEntity.badRequest().body(Map.of("message", "La contrasena debe tener al menos 6 caracteres"));
		}

		// Validamos si existe un usuario con ese email
		if (userRepository.existsByEmail(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Ya existe un usuario con ese email"));
		}

		// Creamos el usuario con contraseña hasheada
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

/*
	// Implementación para futuro
	// Endpoint para añadir usuario
	@PostMapping("/anadir")
	public String doAnadir(Model model) {
		return this.doRegister(model);
	}
	// Endpoint para editar usuario
	@GetMapping("/editar")
	public String doEditar(@RequestParam(value = "id", required = false) Integer id, Model model) {
		model.addAttribute("selectedId", id);
		return this.doRegister(model);
	}
	// Endpoint para guardar usuario
	@PostMapping("/guardar")
	public String doGuardar() {
		return "redirect:/";
	}
*/

	// Métodos auxiliares
	// Limpiador de textos, se asegura de no guardar textos vacíos o llenos de espacios
	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	// Normaliza el email convirtiéndolo a minúsculas y eliminando espacios innecesarios
	private static String normalizeEmail(String email) {
		String trimmed = trimToNull(email);
		return trimmed == null ? null : trimmed.toLowerCase();
	}

	// Valida el formato del email usando una expresión regular simple
	private static boolean isValidEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	// Valida el formato del teléfono permitiendo dígitos, espacios, guiones y signos de más, con una longitud razonable.
	private static boolean isValidPhone(String telefono) {
		return telefono != null && telefono.matches("^[0-9+\\-\\s]{7,20}$");
	}
	
	// Valida que el código postal tenga exactamente 5 dígitos, lo que es común en muchos países.
	private static boolean isValidPostalCode(String cp) {
		return cp != null && cp.matches("^[0-9]{5}$");
	}

	// Métodos relacionados con la gestión de contraseñas usando BCrypt para hashing seguro, verificación de contraseñas y detección de si una contraseña necesita ser migrada a un formato más seguro.
	private static String hashPassword(String rawPassword) {
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
	}

	// Verifica si la contraseña proporcionada coincide con la contraseña almacenada, manejando tanto contraseñas sin formato como contraseñas hashadas con BCrypt.
	private static boolean matchesPassword(String rawPassword, String storedPassword) {
		if (rawPassword == null || storedPassword == null) {
			return false;
		}

		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
			return BCrypt.checkpw(rawPassword, storedPassword);
		}

		return rawPassword.equals(storedPassword);
	}

	// Determina si la contraseña almacenada necesita ser migrada a un formato hashado con BCrypt
	private static boolean needsMigration(String storedPassword) {
		return !(storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
	}

	// Genera un token JWT que incluye el ID del usuario, su email y nombre como claims, con una fecha de expiración basada en la configuración.
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

	// Construye la clave de firma para JWT a partir de la configuración proporcionada. Intenta decodificarla como Base64, y si falla, la hashea con SHA-256 para obtener una clave de longitud adecuada.
	private static SecretKey buildSigningKey(String configuredSecret) {
		try {
			byte[] decoded = Decoders.BASE64.decode(configuredSecret);
			return Keys.hmacShaKeyFor(decoded);
		} catch (RuntimeException ignored) {
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

package es.grupo8.backend.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;


@Controller
public class ApiController {

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected CampaignRepository campaignRepository;

	@Autowired
	protected StoreRepository storeRepository;

	// Usa JWT para autenticación en lugar de sesiones tradicionales, lo que es más adecuado para APIs RESTful y aplicaciones modernas.
	// Hace que el usuario mantenga la sesión activa tras loguearse, incluso después de cerrar el navegador, hasta que el token expire o se revoque.
	@Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms:7200000}")
	private long jwtExpirationMs;

	// Para conectar backend con frontend
	@Value("${app.frontend.base-url:http://localhost:80}")
	private String frontendBaseUrl;

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
	public String doLogin(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "success", required = false) String success,
			Model model) {
				
		model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
		if (error != null && !error.isBlank()) {
			model.addAttribute("loginError", error);
		}
		if (success != null && !success.isBlank()) {
			model.addAttribute("loginSuccess", success);
		}
		return "login";
	}

	// Login con formulario y redirección por rol
	@PostMapping("/login")
	public String doLoginForm(
			@RequestParam(value = "email", required = false) String emailParam,
			@RequestParam(value = "password", required = false) String passwordParam,
			Model model) {

		String email = normalizeEmail(emailParam);
		String password = trimToNull(passwordParam);
		model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");

		if (email == null || password == null) {
			model.addAttribute("loginError", "No existen los datos.");
			return "login";
		}

		UserEntity user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			model.addAttribute("loginError", "No existen los datos.");
			return "login";
		}

		String storedPassword = user.getPassword();
		if (!matchesPassword(password, storedPassword)) {
			model.addAttribute("loginError", "No existen los datos.");
			return "login";
		}

		if (needsMigration(storedPassword)) {
			user.setPassword(hashPassword(password));
			userRepository.save(user);
		}

		String role = resolveRole(user.getIdUser());
		if ("PENDIENTE".equals(role)) {
			model.addAttribute("loginError", "No tiene rol asignado.");
			return "login";
		}

		return "redirect:" + buildFrontendUrl(roleToPath(role));
	}

	// Página de registro
	@GetMapping("/register")
	public String doRegister(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "success", required = false) String success,
			Model model) {
		model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
		if (error != null && !error.isBlank()) {
			model.addAttribute("registerError", error);
		}
		if (success != null && !success.isBlank()) {
			model.addAttribute("registerSuccess", success);
		}
		return "register";
	}

	// Registro con formulario tradicional
	@PostMapping("/register")
	public String doRegisterForm(
			@RequestParam(value = "nombre", required = false) String nombreParam,
			@RequestParam(value = "email", required = false) String emailParam,
			@RequestParam(value = "password", required = false) String passwordParam,
			@RequestParam(value = "confirmPassword", required = false) String confirmPasswordParam,
			@RequestParam(value = "telefono", required = false) String telefonoParam,
			@RequestParam(value = "domicilio", required = false) String domicilioParam,
			@RequestParam(value = "cp", required = false) String cpParam) {

		String nombre = trimToNull(nombreParam);
		String email = normalizeEmail(emailParam);
		String password = trimToNull(passwordParam);
		String confirmPassword = trimToNull(confirmPasswordParam);
		String telefono = trimToNull(telefonoParam);
		String domicilio = trimToNull(domicilioParam);
		String cp = trimToNull(cpParam);

		if (nombre == null || email == null || password == null || confirmPassword == null) {
			return "redirect:/register?error=" + urlEncode("Nombre, email y contrasena son obligatorios");
		}

		if (!isValidEmail(email)) {
			return "redirect:/register?error=" + urlEncode("El email no tiene un formato valido");
		}

		if (password.length() < 6) {
			return "redirect:/register?error=" + urlEncode("La contrasena debe tener al menos 6 caracteres");
		}

		if (!password.equals(confirmPassword)) {
			return "redirect:/register?error=" + urlEncode("Las contrasenas no coinciden");
		}

		if (telefono != null && !isValidPhone(telefono)) {
			return "redirect:/register?error=" + urlEncode("El telefono no tiene un formato valido");
		}

		if (cp != null && !isValidPostalCode(cp)) {
			return "redirect:/register?error=" + urlEncode("El codigo postal no es valido");
		}

		if (userRepository.existsByEmail(email)) {
			return "redirect:/register?error=" + urlEncode("Ya existe un usuario con ese email");
		}

		UserEntity user = new UserEntity();
		user.setName(nombre);
		user.setEmail(email);
		user.setPhone(telefono);
		user.setPassword(hashPassword(password));
		user.setAddress(domicilio);
		user.setPostalCode(cp);

		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			return "redirect:/register?error=" + urlEncode("No se pudo crear la cuenta. Revisa email y codigo postal");
		}

		return "redirect:/login?success=" + urlEncode("Registro correcto. Ya puedes iniciar sesion");
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
					.body(Map.of("message", "No existen los datos"));
		}


		String storedPassword = user.getPassword();
		// Verificamos la contraseña proporcionada con la almacenada
		if (!matchesPassword(password, storedPassword)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Credenciales invalidas"));
		}

		// Si no está en hash la cambiamos
		if (needsMigration(storedPassword)) {
			user.setPassword(hashPassword(password));
			userRepository.save(user);
		}

		// Generamos token JWT con ID usuario, email y nombre
		String token = generateToken(user.getIdUser(), user.getEmail(), user.getName());
		String role = resolveRole(user.getIdUser());

		if ("PENDIENTE".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "No tiene rol asignado."));
		}

		// HashMap porque Map.of() no acepta null (storeId puede ser null)
		Map<String, Object> loginResponse = new HashMap<>();
		loginResponse.put("userId",           user.getIdUser());
		loginResponse.put("nombre",           user.getName());
		loginResponse.put("email",            user.getEmail());
		loginResponse.put("role",             role);
		loginResponse.put("redirectUrl",      buildFrontendUrl(roleToPath(role)));
		loginResponse.put("message",          "Login correcto");
		loginResponse.put("token",            token);
		loginResponse.put("tokenType",        "Bearer");
		loginResponse.put("expiresInSeconds", Math.max(1, jwtExpirationMs / 1000));

		// Si es Responsable de Tienda, incluir storeId para que el frontend sepa a qué tienda ir
		if ("RESPONSABLE_TIENDA".equals(role)) {
			storeRepository.findByIdResponsible_IdUser(user.getIdUser())
					.ifPresent(s -> loginResponse.put("storeId", s.getId()));
		}

		return ResponseEntity.ok(loginResponse);
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
		user.setName(nombre);
		user.setEmail(email);
		user.setPhone(telefono);
		user.setPassword(hashPassword(password));
		user.setAddress(domicilio);
		user.setPostalCode(cp);

		UserEntity createdUser;
		try {
			createdUser = userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", "No se pudo crear la cuenta. Revisa email y codigo postal"));
		}

		String token = generateToken(createdUser.getIdUser(), createdUser.getEmail(), createdUser.getName());

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"userId", createdUser.getIdUser(),
				"nombre", createdUser.getName(),
				"email", createdUser.getEmail(),
				"message", "Registro correcto",
				"token", token,
				"tokenType", "Bearer",
				"expiresInSeconds", Math.max(1, jwtExpirationMs / 1000)
		));
	}

	// Endpoint para obtener el perfil del usuario
	@GetMapping("/api/auth/profile")
	@ResponseBody
	public ResponseEntity<?> getOwnProfile(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		// Obtenemos el ID del usuario
		Integer userId = extractUserIdFromAuthHeader(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token invalido o ausente"));
		}

		// Obtenemos el usuario de la base de datos
		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado"));
		}

		String role = resolveRole(userId);

		return ResponseEntity.ok(Map.of(
				"userId", user.getIdUser(),
				"nombre", user.getName(),
				"email", user.getEmail(),
				"telefono", user.getPhone() == null ? "" : user.getPhone(),
				"domicilio", user.getAddress() == null ? "" : user.getAddress(),
				"cp", user.getPostalCode() == null ? "" : user.getPostalCode(),
				"role", role,
				"redirectUrl", buildFrontendUrl(roleToPath(role))
		));
	}

	// Endpoint para actualizar el perfil del usuario
	@PutMapping("/api/auth/profile")
	@ResponseBody
	public ResponseEntity<?> updateOwnProfile(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestBody Map<String, String> request) {

		Integer userId = extractUserIdFromAuthHeader(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token invalido o ausente"));
		}

		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado"));
		}

		String email = normalizeEmail(request == null ? null : request.get("email"));
		String telefono = trimToNull(request == null ? null : request.get("telefono"));
		String domicilio = trimToNull(request == null ? null : request.get("domicilio"));
		String cp = trimToNull(request == null ? null : request.get("cp"));

		if (email == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email es obligatorio"));
		}

		if (!isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email no tiene un formato valido"));
		}

		if (telefono != null && !isValidPhone(telefono)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El telefono no tiene un formato valido"));
		}

		if (cp != null && !isValidPostalCode(cp)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El codigo postal no es valido"));
		}

		if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Ya existe un usuario con ese email"));
		}

		user.setEmail(email);
		user.setPhone(telefono);
		user.setAddress(domicilio);
		user.setPostalCode(cp);

		UserEntity updated;
		try {
			updated = userRepository.save(user);
		} catch (DataIntegrityViolationException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", "No se pudo actualizar el perfil"));
		}

		String role = resolveRole(userId);

		return ResponseEntity.ok(Map.of(
				"userId", updated.getIdUser(),
				"nombre", updated.getName(),
				"email", updated.getEmail(),
				"telefono", updated.getPhone() == null ? "" : updated.getPhone(),
				"domicilio", updated.getAddress() == null ? "" : updated.getAddress(),
				"cp", updated.getPostalCode() == null ? "" : updated.getPostalCode(),
				"role", role,
				"redirectUrl", buildFrontendUrl(roleToPath(role)),
				"message", "Perfil actualizado correctamente"
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

	private Integer extractUserIdFromAuthHeader(String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}

		try {
			String subject = Jwts.parser()
					.verifyWith(signingKey)
					.build()
					.parseSignedClaims(authHeader.substring(7).trim())
					.getPayload()
					.getSubject();
			return subject == null ? null : Integer.valueOf(subject);
		} catch (Exception ex) {
			return null;
		}
	}

	// Endpoint para obtener todas las campañas
	@GetMapping("/api/campaigns")
	@ResponseBody
	public ResponseEntity<?> getCampaigns(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		Integer userId = extractUserIdFromAuthHeader(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token invalido o ausente"));
		}

		String role = resolveRole(userId);
		if (!"ADMINISTRADOR".equals(role) && !"COORDINADOR".equals(role) && !"CAPITAN".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "No tienes permiso para ver las campañas"));
		}

		List<Campaign> campaigns = campaignRepository.findAll();
		List<Map<String, Object>> result = campaigns.stream()
				.map(c -> {
					Map<String, Object> map = new HashMap<>();
					map.put("id", c.getId());
					map.put("name", c.getName());
					map.put("startDate", c.getStartDate());
					map.put("endDate", c.getEndDate());
					map.put("type", c.getIdType() != null ? c.getIdType().getName() : "");
					return map;
				})
				.collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	// Endpoint para obtener tiendas de una campaña
	@GetMapping("/api/campaigns/{id}/stores")
	@ResponseBody
	public ResponseEntity<?> getCampaignStores(
			@PathVariable("id") Integer campaignId,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		Integer userId = extractUserIdFromAuthHeader(authHeader);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Token invalido o ausente"));
		}

		String role = resolveRole(userId);
		if (!"ADMINISTRADOR".equals(role) && !"COORDINADOR".equals(role) && !"CAPITAN".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "No tienes permiso para ver las tiendas"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "Campaña no encontrada"));
		}

		List<Map<String, Object>> stores = campaign.getStores().stream()
				.map(s -> {
					Map<String, Object> map = new HashMap<>();
					map.put("id", s.getId());
					map.put("name", s.getName());
					map.put("address", s.getAddress() != null ? s.getAddress() : "");
					return map;
				})
				.collect(Collectors.toList());

		return ResponseEntity.ok(stores);
	}

	private String resolveRole(Integer userId) {
		if (userRepository.isAdmin(userId)) {
			return "ADMINISTRADOR";
		}

		if (userRepository.isCoordinator(userId)) {
			return "COORDINADOR";
		}

		if (userRepository.isCaptain(userId)) {
			return "CAPITAN";
		}

		if (userRepository.isPartnerEntityManager(userId)) {
			return "COLABORADOR";
		}

		if (storeRepository.existsByIdResponsible_IdUser(userId)) {
			return "RESPONSABLE_TIENDA";
		}

		return "PENDIENTE";
	}

	private static String roleToPath(String role) {
		if ("ADMINISTRADOR".equals(role)) {
			return "/admin.html";
		}

		if ("COORDINADOR".equals(role)) {
			return "/coordinator.html";
		}

		if ("CAPITAN".equals(role)) {
			return "/captain.html";
		}

		if ("COLABORADOR".equals(role)) {
			return "/collaborator.html";
		}

		if ("RESPONSABLE_TIENDA".equals(role)) {
			return "/responsible-store.html";
		}

		return "/login.html";
	}

	private String buildFrontendUrl(String path) {
		String base = frontendBaseUrl == null ? "http://localhost:80" : frontendBaseUrl.trim();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}

		if (path.startsWith("/")) {
			return base + path;
		}

		return base + "/" + path;
	}

	private static String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
package es.grupo8.backend.controllers;

import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.RequestParam;
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



	@Value("${app.jwt.secret:change-this-secret-in-production-change-this-secret-in-production}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms:7200000}")
	private long jwtExpirationMs;


	@Value("${app.frontend.base-url:http://localhost:80}")
	private String frontendBaseUrl;

	private SecretKey signingKey;

	@PostConstruct
	public void initJwt() {
		signingKey = buildSigningKey(jwtSecret);
	}





	@GetMapping({"/", "/index"})
	public String doInit(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Home");
		return "index";
	}


	@GetMapping("/login")
	public String doLogin(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Login");
		return "login";
	}


	@PostMapping("/login")
	public String doLoginForm(
			@RequestParam(value = "email", required = false) String emailParam,
			@RequestParam(value = "password", required = false) String passwordParam) {

		final String invalidCredentialsMessage = "Those credentials do not exist in the database";

		String email = normalizeEmail(emailParam);
		String password = trimToNull(passwordParam);

		if (email == null || password == null) {
			return "redirect:/login?error=" + urlEncode("Email and password are required");
		}

		UserEntity user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			return "redirect:/login?error=" + urlEncode(invalidCredentialsMessage);
		}

		String storedPassword = user.getPassword();
		if (!matchesPassword(password, storedPassword)) {
			return "redirect:/login?error=" + urlEncode(invalidCredentialsMessage);
		}

		if (needsMigration(storedPassword)) {
			user.setPassword(hashPassword(password));
			userRepository.save(user);
		}

		String role = resolveRole(user.getIdUser());
		return "redirect:" + buildFrontendUrl(roleToPath(role));
	}


	@GetMapping("/register")
	public String doRegister(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "success", required = false) String success,
			Model model) {
		model.addAttribute("pageTitle", "Bancosol | Create Account");
		if (error != null && !error.isBlank()) {
			model.addAttribute("registerError", error);
		}
		if (success != null && !success.isBlank()) {
			model.addAttribute("registerSuccess", success);
		}
		return "register";
	}


	@PostMapping("/register")
	public String doRegisterForm(
			@RequestParam(value = "name", required = false) String nameParam,
			@RequestParam(value = "email", required = false) String emailParam,
			@RequestParam(value = "password", required = false) String passwordParam,
			@RequestParam(value = "confirmPassword", required = false) String confirmPasswordParam,
			@RequestParam(value = "phone", required = false) String phoneParam,
			@RequestParam(value = "address", required = false) String addressParam,
			@RequestParam(value = "postalCode", required = false) String postalCodeParam) {

		String name = trimToNull(nameParam);
		String email = normalizeEmail(emailParam);
		String password = trimToNull(passwordParam);
		String confirmPassword = trimToNull(confirmPasswordParam);
		String phone = trimToNull(phoneParam);
		String address = trimToNull(addressParam);
		String postalCode = trimToNull(postalCodeParam);

		if (name == null || email == null || password == null || confirmPassword == null) {
			return "redirect:/register?error=" + urlEncode("Name, email, and password are required");
		}

		if (!isValidEmail(email)) {
			return "redirect:/register?error=" + urlEncode("Email format is invalid");
		}

		if (password.length() < 6) {
			return "redirect:/register?error=" + urlEncode("Password must be at least 6 characters long");
		}

		if (!password.equals(confirmPassword)) {
			return "redirect:/register?error=" + urlEncode("Passwords do not match");
		}

		if (phone != null && !isValidPhone(phone)) {
			return "redirect:/register?error=" + urlEncode("Phone format is invalid");
		}

		if (postalCode != null && !isValidPostalCode(postalCode)) {
			return "redirect:/register?error=" + urlEncode("Postal code is invalid");
		}

		if (userRepository.existsByEmail(email)) {
			return "redirect:/register?error=" + urlEncode("A user with that email already exists");
		}

		UserEntity user = new UserEntity();
		user.setName(name);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPassword(hashPassword(password));
		user.setAddress(address);
		user.setPostalCode(postalCode);

		userRepository.save(user);
		return "redirect:/login?success=" + urlEncode("Registration successful. You can now sign in");
	}



	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {


		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));


		if (email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
		}


		UserEntity user = userRepository.findByEmail(email).orElse(null);
		

		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Invalid credentials"));
		}


		String storedPassword = user.getPassword();

		if (!matchesPassword(password, storedPassword)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Invalid credentials"));
		}


		if (needsMigration(storedPassword)) {
			user.setPassword(hashPassword(password));
			userRepository.save(user);
		}


		String token = generateToken(user.getIdUser(), user.getEmail(), user.getName());
		String role = resolveRole(user.getIdUser());

		return ResponseEntity.ok(Map.of(
				"userId", user.getIdUser(),
				"name", user.getName(),
				"email", user.getEmail(),
				"role", role,
				"redirectUrl", buildFrontendUrl(roleToPath(role)),
				"message", "Login successful",
				"token", token,
				"tokenType", "Bearer",
				"expiresInSeconds", Math.max(1, jwtExpirationMs / 1000)
		));
	}




	@PostMapping("/api/auth/register")
	@ResponseBody
	public ResponseEntity<?> register(@RequestBody Map<String, String> request) {

		String name = trimToNull(request == null ? null : request.get("name"));
		String email = normalizeEmail(request == null ? null : request.get("email"));
		String password = trimToNull(request == null ? null : request.get("password"));
		String phone = trimToNull(request == null ? null : request.get("phone"));
		String address = trimToNull(request == null ? null : request.get("address"));
		String postalCode = trimToNull(request == null ? null : request.get("postalCode"));


		if (name == null || email == null || password == null || phone == null || address == null || postalCode == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Name, email, phone, password, address, and postal code are required"));
		}


		if (!isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email format is invalid"));
		}


		if (!isValidPhone(phone)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Phone format is invalid"));
		}


		if (!isValidPostalCode(postalCode)) {
			return ResponseEntity.badRequest().body(Map.of("message", "Postal code is invalid"));
		}


		if (password.length() < 6) {
			return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters long"));
		}


		if (userRepository.existsByEmail(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "A user with that email already exists"));
		}


		UserEntity user = new UserEntity();
		user.setName(name);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPassword(hashPassword(password));
		user.setAddress(address);
		user.setPostalCode(postalCode);

		UserEntity createdUser = userRepository.save(user);
		String token = generateToken(createdUser.getIdUser(), createdUser.getEmail(), createdUser.getName());

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"userId", createdUser.getIdUser(),
				"name", createdUser.getName(),
				"email", createdUser.getEmail(),
				"message", "Registration successful",
				"token", token,
				"tokenType", "Bearer",
				"expiresInSeconds", Math.max(1, jwtExpirationMs / 1000)
		));
	}

/*


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
*/



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


	private static boolean isValidPhone(String phone) {
		return phone != null && phone.matches("^[0-9+\\-\\s]{7,20}$");
	}
	

	private static boolean isValidPostalCode(String postalCode) {
		return postalCode != null && postalCode.matches("^[0-9]{5}$");
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


	private String generateToken(Integer userId, String email, String name) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("name", name)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(jwtExpirationMs)))
				.signWith(signingKey)
				.compact();
	}


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
				throw new IllegalStateException("Failed to initialize JWT key", ex);
			}
		}
	}

	private String resolveRole(Integer userId) {
		if (userRepository.isAdmin(userId)) {
			return "ADMIN";
		}

		if (userRepository.isCoordinator(userId)) {
			return "COORDINATOR";
		}

		if (userRepository.isCaptain(userId)) {
			return "CAPTAIN";
		}

		if (userRepository.isPartnerEntityManager(userId)) {
			return "COLLABORATOR";
		}

		return "COLLABORATOR";
	}

	private static String roleToPath(String role) {
		if ("ADMIN".equals(role)) {
			return "/admin.html";
		}

		if ("COORDINATOR".equals(role)) {
			return "/coordinator.html";
		}

		if ("CAPTAIN".equals(role)) {
			return "/captain.html";
		}

		return "/collaborator.html";
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

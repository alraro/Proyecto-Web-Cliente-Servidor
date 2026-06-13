/*
*
* Autores:
*	- Hugo Herrero González: 70%
*   - IA Generativa: 30%
*/
package es.grupo8.backend.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.AuthResponseDTO;
import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.UserAuthMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserAuthMapper userAuthMapper;


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



    // Login
    public AuthResponseDTO login(String emailParam, String passwordParam) {
        String email = UtilsService.normalizeEmail(emailParam);
        String password = UtilsService.trimToNull(passwordParam);

        if(email == null || password == null) return null;

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;

        String stored = user.getPassword();
        if(!UtilsService.matchesPassword(password, stored)) return null;

        if(UtilsService.needsMigration(stored)) {
            user.setPassword(UtilsService.hashPassword(password));
            userRepository.save(user);
        }

        String role = resolveRole(user.getIdUser());
        String token = generateToken(user.getIdUser(), user.getEmail(), user.getName());

        Integer storeId = null;
        if ("RESPONSABLE_TIENDA".equals(role)) {
			storeId = storeRepository.findByIdResponsible_IdUser(user.getIdUser()).map(store -> store.getId()).orElse(null);
		}

        return userAuthMapper.toLoginResponse(user, token, role, buildFrontendUrl(roleToPath(role)), Math.max(1, jwtExpirationMs / 1000), storeId);

    }


    // Register
    public AuthResponseDTO register(String nombreParam, String emailParam, String passwordParam, String confirmPasswordParam, String telefonoParam, String domicilioParam, String cpParam) {

        UserEntity user = new UserEntity();

		if (!passwordParam.equals(confirmPasswordParam)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        user.setName(UtilsService.trimToNull(nombreParam));
        user.setEmail(UtilsService.normalizeEmail(emailParam));
        user.setPhone(UtilsService.trimToNull(telefonoParam));
        user.setPassword(UtilsService.hashPassword(UtilsService.trimToNull(passwordParam)));
        user.setAddress(UtilsService.trimToNull(domicilioParam));
        user.setPostalCode(UtilsService.trimToNull(cpParam));
		
		// Comprobamos datos obligatorios como nombre, email y contraseña
		if (user.getName() == null || user.getEmail() == null || user.getPassword() == null) {
			throw new IllegalArgumentException("Nombre, email y contraseña son obligatorios");
		}

		// Validamos formato de email
		if (!UtilsService.isValidEmail(user.getEmail())) {
			throw new IllegalArgumentException("El email no tiene un formato valido");
		}

		// Validamos formato telefono
		if (user.getPhone() != null && !UtilsService.isValidPhone(user.getPhone())) {
			throw new IllegalArgumentException("El telefono no tiene un formato valido");
		}

		// Validamos formato código postal
		if (user.getPostalCode() != null && !UtilsService.isValidPostalCode(user.getPostalCode())) {
			throw new IllegalArgumentException("El codigo postal no es valido");
		}

		// Validamos tamaño minimo contraseña
		if (user.getPassword().length() < 6) {
			throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres");
		}

		// Validamos si existe un usuario con ese email
		if (emailExists(user.getEmail())) {
			throw new IllegalStateException("Ya existe un usuario con ese email");
		}

        UserEntity created = userRepository.save(user);
        String token = generateToken(created.getIdUser(), created.getEmail(), created.getName());
        
        return userAuthMapper.toRegisterResponse(created, token, Math.max(1, jwtExpirationMs / 1000));
    }


    // Profile
    public ProfileDTO getProfile(Integer userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if(user == null) return null;

        String role = resolveRole(userId);
        
        return userAuthMapper.toProfileDTO(user, role, buildFrontendUrl(roleToPath(role)), null);
    }


    public ProfileDTO updateProfile(Integer userId, String emailParam, String passwordParam, String confirmPasswordParam, String telefonoParam, String domicilioParam, String cpParam) {

        UserEntity user = userRepository.findById(userId).orElse(null);
        if(user == null) return null;

        user.setEmail(UtilsService.normalizeEmail(emailParam));
        user.setPhone(UtilsService.trimToNull(telefonoParam));
        user.setAddress(UtilsService.trimToNull(domicilioParam));
        user.setPostalCode(UtilsService.trimToNull(cpParam));

		String newPassword = UtilsService.trimToNull(passwordParam);
		String newConfirm = UtilsService.trimToNull(confirmPasswordParam);

		if (newPassword != null) {
			if (newPassword.length() < 6) {
				throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres");
			}
			if(!newPassword.equals(newConfirm)){
				throw new IllegalArgumentException("La contraseña no coincide con la confirmación");
			}

			user.setPassword(UtilsService.hashPassword(newPassword));
		}

		if (user.getEmail() == null) {
			throw new IllegalArgumentException("El email es obligatorio");
		}

		if (!UtilsService.isValidEmail(user.getEmail())) {
			throw new IllegalArgumentException("El email no tiene un formato valido");
		}

		if (user.getPhone() != null && !UtilsService.isValidPhone(user.getPhone())) {
			throw new IllegalArgumentException("El telefono no tiene un formato valido");
		}

		if (user.getPostalCode() != null && !UtilsService.isValidPostalCode(user.getPostalCode())) {
			throw new IllegalArgumentException("El codigo postal no es valido");
		}

        UserEntity updated = userRepository.save(user);
        String role = resolveRole(userId);

        return userAuthMapper.toProfileDTO(updated, role, buildFrontendUrl(roleToPath(role)), "Perfil actualizado correctamente");
    }


    // JWT
    public Integer extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        try {
            String subject = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(authHeader.substring(7).trim()).getPayload().getSubject();

            return subject == null ? null : Integer.valueOf(subject);
        } catch (Exception ex) {
            return null;
        }
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

	// Devuelve el rol del usuario
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

	// Devuelve el path que debe seguir según el rol que tiene
	private static String roleToPath(String role) {
		if ("ADMINISTRADOR".equals(role)) {
			return "/admin.html";
		}

		if ("COORDINADOR".equals(role)) {
			return "/coordinator-dashboard.html";
		}

		if ("CAPITAN".equals(role)) {
			return "/captain-dashboard.html";
		}

		if ("COLABORADOR".equals(role)) {
			return "/collaborator.html";
		}

		if ("RESPONSABLE_TIENDA".equals(role)) {
			return "/responsible-store.html";
		}

		return "/login.html";
	}

	// Creamos URL del frontend
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

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean emailExistsForOther(String email, String current){
		return !email.equalsIgnoreCase(current) && userRepository.existsByEmail(email);
	}

    
}

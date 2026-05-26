package es.grupo8.backend.controllers;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.LoginResponseDTO;
import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.dto.RegisterResponseDTO;
import es.grupo8.backend.services.AuthService;

@RestController
public class AuthController {
    
    @Autowired
    private AuthService authService;


    @PostMapping("/api/auth/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

		// Sacamos email y contraseña
		String email = AuthService.normalizeEmail(request == null ? null : request.get("email"));
		String password = AuthService.trimToNull(request == null ? null : request.get("password"));

		// Validamos el email y contraseña
		if (email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Email y contraseña son obligatorios"));
		}

		LoginResponseDTO dto = authService.login(email, password);
		// Si no existe el usuario, fuera
		if (dto == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "No existen los datos"));
		}
        if ("PENDIENTE".equals(dto.getRole())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "No tiene rol asignado."));
		}

        return ResponseEntity.ok(dto);
	}
    

    // Endpoint para registro
	@PostMapping("/api/auth/register")
	@ResponseBody
	public ResponseEntity<?> register(@RequestBody Map<String, String> request) {

		String nombre = AuthService.trimToNull(request == null ? null : request.get("nombre"));
		String email = AuthService.normalizeEmail(request == null ? null : request.get("email"));
		String password = AuthService.trimToNull(request == null ? null : request.get("password"));
		String telefono = AuthService.trimToNull(request == null ? null : request.get("telefono"));
		String domicilio = AuthService.trimToNull(request == null ? null : request.get("domicilio"));
		String cp = AuthService.trimToNull(request == null ? null : request.get("cp"));

		// Comprobamos datos obligatorios
		if (nombre == null || email == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Nombre, email y contrasena son obligatorios"));
		}

		// Validamos formato de email
		if (!AuthService.isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email no tiene un formato valido"));
		}

		// Validamos formato telefono
		if (telefono != null && !AuthService.isValidPhone(telefono)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El telefono no tiene un formato valido"));
		}

		// Validamos formato código postal
		if (cp != null && !AuthService.isValidPostalCode(cp)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El codigo postal no es valido"));
		}

		// Validamos tamaño minimo contraseña
		if (password.length() < 6) {
			return ResponseEntity.badRequest().body(Map.of("message", "La contrasena debe tener al menos 6 caracteres"));
		}

		// Validamos si existe un usuario con ese email
		if (authService.emailExists(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Ya existe un usuario con ese email"));
		}

        try {
            RegisterResponseDTO dto = authService.register(nombre, email, password, telefono, domicilio, cp);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se pudo crear la cuenta. Revisa email y codigo postal"));
        }
	}



    // Endpoint para obtener el perfil del usuario
	@GetMapping("/api/auth/profile")
	@ResponseBody
	public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String auth) {

		// Obtenemos el ID del usuario
		Integer userId = authService.extractUserIdFromToken(auth);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token invalido o ausente"));
		}

		ProfileDTO dto = authService.getProfile(userId);
        if(dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));
        }

        return ResponseEntity.ok(dto);
	}

	// Endpoint para actualizar el perfil del usuario
	@PutMapping("/api/auth/profile")
	@ResponseBody
	public ResponseEntity<?> updateOwnProfile(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody Map<String, String> request) {

        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token invalido o ausente"));
		}

		String email = AuthService.normalizeEmail(request == null ? null : request.get("email"));
		String telefono = AuthService.trimToNull(request == null ? null : request.get("telefono"));
		String domicilio = AuthService.trimToNull(request == null ? null : request.get("domicilio"));
		String cp = AuthService.trimToNull(request == null ? null : request.get("cp"));

		if (email == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email es obligatorio"));
		}

		if (!AuthService.isValidEmail(email)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El email no tiene un formato valido"));
		}

		if (telefono != null && !AuthService.isValidPhone(telefono)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El telefono no tiene un formato valido"));
		}

		if (cp != null && !AuthService.isValidPostalCode(cp)) {
			return ResponseEntity.badRequest().body(Map.of("message", "El codigo postal no es valido"));
		}

		ProfileDTO current = authService.getProfile(userId);
        if (current == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));
        }
        if (authService.emailExistsForOther(email, current.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Ya existe un usuario con ese email"));
        }


        try {
            ProfileDTO dto = authService.updateProfile(userId, email, telefono, domicilio, cp);
            return ResponseEntity.ok(dto);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se pudo actualizar el perfil. Revisa email y codigo postal"));
        }
	}




}

/*
* RestController para la autenticación de usuario, creación de cuenta y gestión de perfil.
*
* Autores:
*	- Hugo Herrero González: 80%
*	- IA Generativa: 20%
*/
package es.grupo8.backend.controllers.rest;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.AuthResponseDTO;
import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.services.AuthService;

// Devolver datos en formato JSON, no vistas
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthResponseDTO request) {

		// Sacamos email y contraseña
		String email = request.getEmail();
		String password = request.getPassword();

		AuthResponseDTO dto = authService.login(email, password);
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
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody AuthResponseDTO request) {

		String nombre = request.getNombre();
		String email = request.getEmail();
		String password = request.getPassword();
		String telefono = request.getPhone();
		String domicilio = request.getAddress();
		String cp = request.getPostalCode();

        try {
            AuthResponseDTO dto = authService.register(nombre, email, password, telefono, domicilio, cp);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se pudo crear la cuenta. Revisa email y codigo postal"));
        }
	}



    // Endpoint para obtener el perfil del usuario
	@GetMapping("/profile")
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
	@PutMapping("/profile")
	public ResponseEntity<?> updateOwnProfile(@RequestHeader(value = "Authorization", required = false) String auth, 
											  @RequestBody ProfileDTO request) {

        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token invalido o ausente"));
		}

		String email = request.getEmail();
		String password = request.getPassword();
		String confirmPassword = request.getConfirmPassword();
		String telefono = request.getTelefono();
		String domicilio = request.getDomicilio();
		String cp = request.getCp();

		ProfileDTO current = authService.getProfile(userId);
        if (current == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));
        }
        if (authService.emailExistsForOther(email, current.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Ya existe un usuario con ese email"));
        }


        try {
            ProfileDTO dto = authService.updateProfile(userId, email, password, confirmPassword, telefono, domicilio, cp);

            return ResponseEntity.ok(dto);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
			
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se pudo actualizar el perfil. Revisa email y codigo postal"));
        }
	}
}

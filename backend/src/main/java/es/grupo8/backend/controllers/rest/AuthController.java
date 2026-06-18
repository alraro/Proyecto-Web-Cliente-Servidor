/*
* RestController para la autenticación de usuario, creación de cuenta y gestión de perfil.
*
* Autores:
*	- Hugo Herrero González: 80%
*	- IA Generativa: 20%
*/
package es.grupo8.backend.controllers.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	public AuthResponseDTO login(@RequestBody AuthResponseDTO request) {

		// Sacamos email y contraseña
		String email = request.getEmail();
		String password = request.getPassword();

		AuthResponseDTO dto = authService.login(email, password);
		// Si no existe el usuario, fuera
		if (dto == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No existen los datos");
		}
        if ("PENDIENTE".equals(dto.getRole())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene rol asignado.");
		}

        return dto;
	}
    

    // Endpoint para registro
	@PostMapping("/register")
	public AuthResponseDTO register(@RequestBody AuthResponseDTO request) {

		String nombre = request.getNombre();
		String email = request.getEmail();
		String password = request.getPassword();
		String confirmPassword = request.getConfirmPassword();
		String telefono = request.getPhone();
		String domicilio = request.getAddress();
		String cp = request.getPostalCode();

        try {
            AuthResponseDTO dto = authService.register(nombre, email, password, confirmPassword, telefono, domicilio, cp);

			return dto;

		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

		} catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo crear la cuenta. Revisa los datos");
        }
	}



    // Endpoint para obtener el perfil del usuario
	@GetMapping("/profile")
	public ProfileDTO getProfile(@RequestHeader(value = "Authorization", required = false) String auth) {

		// Obtenemos el ID del usuario
		Integer userId = authService.extractUserIdFromToken(auth);
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
		}

		ProfileDTO dto = authService.getProfile(userId);
        if(dto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        return dto;
	}
	

	// Endpoint para actualizar el perfil del usuario
	@PutMapping("/profile")
	public ProfileDTO updateOwnProfile(@RequestHeader(value = "Authorization", required = false) String auth, 
									   @RequestBody ProfileDTO request) {

        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
		}

		String email = request.getEmail();
		String password = request.getPassword();
		String confirmPassword = request.getConfirmPassword();
		String telefono = request.getTelefono();
		String domicilio = request.getDomicilio();
		String cp = request.getCp();

		ProfileDTO current = authService.getProfile(userId);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        if (authService.emailExistsForOther(email, current.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }


        try {
            ProfileDTO dto = authService.updateProfile(userId, email, password, confirmPassword, telefono, domicilio, cp);

            return dto;
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
			
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo actualizar el perfil. Revisa los datos");
        }
	}

	@PostMapping("/admin/users")
	public AuthResponseDTO createUser(@RequestHeader(value = "Authorization", required = false) String auth,
									  @RequestBody AuthResponseDTO request){

		Integer adminId = authService.extractUserIdFromToken(auth);
		if(adminId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
		}

		try {
			AuthResponseDTO dto = authService.register(
				request.getNombre(),
				request.getEmail(),
				request.getPassword(),
				request.getConfirmPassword(),
				request.getPhone(),
				request.getAddress(),
				request.getPostalCode()
			);

			return dto;

		} catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	} 


}

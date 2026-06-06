/*
*
*
* Autores:
*	- Hugo Herrero González: 90%
*   - IA Generativa: 10%
*/
package es.grupo8.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.grupo8.backend.dto.AuthResponseDTO;

import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.services.AuthService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ViewsController {

    @Autowired
    private AuthService authService;

    // Pagina de inicio, sirve tanto el / como el index, es lo mismo
	@GetMapping({"/", "/index"})
	public String doInit(Model model) {
		model.addAttribute("pageTitle", "Bancosol | Inicio");
		return "index";
	}


    // Página de login
	@GetMapping("/login")
	public String doLogin(@RequestParam(value = "error", required = false) String error,
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

	@PostMapping("/login")
	public String submitLogin(@RequestParam(value = "email", required = false) String email,
							  @RequestParam(value = "password", required = false) String password,
							  Model model,
							  HttpSession session) {

		AuthResponseDTO dto = authService.login(email, password);

		if (dto == null) {
			model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
			model.addAttribute("loginError", "No existen los datos");
			return "login";
		}

		if ("PENDIENTE".equals(dto.getRole())) {
			model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
			model.addAttribute("loginError", "No tiene rol asignado.");
			return "login";
		}

		session.setAttribute("token", dto.getToken());
		session.setAttribute("userID", dto.getId());
		session.setAttribute("nombre", dto.getNombre());
		session.setAttribute("role", dto.getRole());
		if (dto.getStoreId() != null) {
			session.setAttribute("storeId", dto.getStoreId());
		} else {
			session.removeAttribute("storeId");
		}

		return "redirect:" + resolveRolePath(dto.getRole());
	}



    // Página de registro
	@GetMapping("/register")
	public String doRegister(@RequestParam(value = "error", required = false) String error,
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

    @PostMapping("/register")
    public String submitRegister(@RequestParam(value = "nombre", required = false) String nombre,
                                 @RequestParam(value = "email", required = false) String email,
                                 @RequestParam(value = "telefono", required = false) String telefono,
                                 @RequestParam(value = "password", required = false) String password,
                                 @RequestParam(value = "domicilio", required = false) String domicilio,
                                 @RequestParam(value = "cp", required = false) String cp,
                                 Model model) {

		try {
			AuthResponseDTO dto = authService.register(nombre, email, password, telefono, domicilio, cp);

			if (dto == null) {
				model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
				model.addAttribute("registerError", "No se han podido registrar los datos.");
				return "register";
			}

			return "redirect:/login";

		} catch (IllegalArgumentException e) {
			model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
			model.addAttribute("registerError", e.getMessage());
			return "register";

		} catch (IllegalStateException e) {
			model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
			model.addAttribute("registerError", e.getMessage());
			return "register";
		}
    }

    @GetMapping("logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
		model.addAttribute("pageTitle", "Bancosol | Inicio de sesión");
        
		return "login";
    }

	@GetMapping("/edit")
	public String editProfile(HttpSession session,
							  Model model) {
		Integer userId = (Integer) session.getAttribute("userID");
		if (userId == null) {
			return "redirect:/login";
		}

		ProfileDTO dto = authService.getProfile(userId);
		if(dto == null) {
			model.addAttribute("editError", "No se han podido cargar los datos del perfil.");
			return "edit";
		}

		model.addAttribute("dto", dto);

		return "edit";
	}

	@PostMapping("/edit")
	public String submitEditProfile(@RequestParam(value = "email", required = false) String email,
									@RequestParam(value = "password", required = false) String password,
									@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
									@RequestParam(value = "telefono", required = false) String telefono,
									@RequestParam(value = "domicilio", required = false) String domicilio,
									@RequestParam(value = "cp", required = false) String cp,
									HttpSession session,
									Model model) {
		Integer userId = (Integer) session.getAttribute("userID");

		if(userId == null){
			return "redirect:/login";
		}
		
		try {
			ProfileDTO dto = authService.updateProfile(userId, email, password, confirmPassword, telefono, domicilio, cp);

			String role = (String) session.getAttribute("role");

			if(dto == null) {
				model.addAttribute("editError", "No se pueden actualizar los datos");
				return "edit";
			}

			session.setAttribute("email", dto.getEmail());

			return "redirect:" + resolveRolePath(role);

		} catch (IllegalStateException e) {
			model.addAttribute("editError", e.getMessage());
			return "edit";
		}

	}

	@GetMapping("/cancel-edit")
	public String cancelEdit(HttpSession session) {
		String role = (String) session.getAttribute("role");

		return "redirect:" + resolveRolePath(role);
	}


	@GetMapping("/coordinator")
	public String coordinator() {
		return "coordinator";
	}

	@GetMapping("/coordinator-dashboard")
	public String coordinatorDashboard() { return "coordinator-dashboard"; }

	@GetMapping("/coordinator-campaigns")
	public String coordinatorCampaigns() { return "coordinator-campaigns"; }

	@GetMapping("/coordinator-stores")
	public String coordinatorStores() { return "coordinator-stores"; }

	@GetMapping("/coordinator-captains")
	public String coordinatorCaptains() { return "coordinator-captains"; }

	@GetMapping("/coordinator-volunteers")
	public String coordinatorVolunteers() { return "coordinator-volunteers"; }

	@GetMapping("/coordinator-collaborators")
	public String coordinatorCollaborators() { return "coordinator-collaborators"; }

	@GetMapping("/coordinator-entities")
	public String coordinatorEntities() { return "coordinator-entities"; }

	@GetMapping("/captain")
	public String captain() {
		return "captain";
	}

	@GetMapping("/captain-dashboard")
	public String captainDashboard() { return "captain-dashboard"; }

	@GetMapping("/captain-stores")
	public String captainStores() { return "captain-stores"; }

	@GetMapping("/captain-incidents")
	public String captainIncidents() { return "captain-incidents"; }

	@GetMapping("/captain-attendance")
	public String captainAttendance() { return "captain-attendance"; }

	@GetMapping("/collaborator")
	public String collaborator() {
		return "colaborator";
	}

	private String resolveRolePath(String role) {
		if ("ADMINISTRADOR".equals(role)) {
			return "/admin";
		}

		if ("COORDINADOR".equals(role)) {
			return "/coordinator";
		}

		if ("CAPITAN".equals(role)) {
			return "/captain";
		}

		if ("COLABORADOR".equals(role)) {
			return "/collaborator";
		}

		if ("RESPONSABLE_TIENDA".equals(role)) {
			return "/responsible-store";
		}

		return "/login";
	}

}

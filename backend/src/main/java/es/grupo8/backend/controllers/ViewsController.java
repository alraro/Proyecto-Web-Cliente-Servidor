package es.grupo8.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.grupo8.backend.dto.RegisterResponseDTO;

import es.grupo8.backend.dto.LoginResponseDTO;
import es.grupo8.backend.services.AuthService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ViewsController {

    @Autowired
    private AuthService authService;

    // Pagina de inicio
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

		LoginResponseDTO dto = authService.login(email, password);

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

        RegisterResponseDTO dto = authService.register(nombre, email, password, telefono, domicilio, cp);

        if (dto == null) {
            model.addAttribute("pageTitle", "Bancosol | Crear cuenta");
            model.addAttribute("registerError", "No se han podido registrar los datos.");
            return "register";
        }

        return "redirect:/login";
    }

    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate();
        
		return "login";
    }


	@GetMapping("/coordinator")
	public String coordinator() {
		return "coordinator";
	}

	@GetMapping("/captain")
	public String captain() {
		return "captain";
	}

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

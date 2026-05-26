package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ViewsController {

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
    
}

package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/inicio")
    public String mostrarPaginaInicio(Model model) {
        model.addAttribute("titulo", "Backend SSR - Página de Inicio");
        model.addAttribute("mensaje", "¡Esta es la página de inicio del backend con SSR usando Spring Boot!");
        model.addAttribute("usuario", "Usuario Random");

        return "index";
    }
}

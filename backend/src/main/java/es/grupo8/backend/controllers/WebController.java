package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/inicio")
    public String mostrarPaginaInicio(Model model) {
        model.addAttribute("titulo", "Panel de Gestión Universitaria");
        model.addAttribute("usuario", "Alfonso");

        return "index";
    }
}

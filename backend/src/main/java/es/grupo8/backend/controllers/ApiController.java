package es.grupo8.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    @GetMapping("/ejemplo")
    public List<String> obtenerEstado() {
        return List.of("Backend funcionando correctamente", "API de ejemplo");
    }
}

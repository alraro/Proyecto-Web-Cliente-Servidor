package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.security.AdminGuard;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// Controlador MVC para la página de solicitudes de alta de capitanes.
// Carga las solicitudes pendientes para que el administrador pueda aprobarlas o rechazarlas.
@Controller
@AllArgsConstructor
public class AdminCaptainRequestController {

    private final AdminGuard adminGuard;
    private final CaptainRequestRepository captainRequestRepository;

    // Devuelve true si el usuario en sesión es administrador
    private boolean isAdmin(HttpSession session) {
        Object token = session.getAttribute("token");
        if (token == null) return false;
        return adminGuard.isAdmin("Bearer " + token);
    }

    // Listado de solicitudes de alta de capitanes pendientes
    @GetMapping("/admin-captain-requests")
    public String adminCaptainRequests(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<CaptainRequest> pendingRequests = captainRequestRepository.findByStatus("PENDIENTE");
        model.addAttribute("pendingRequests", pendingRequests);
        return "admin-captain-requests";
    }
}

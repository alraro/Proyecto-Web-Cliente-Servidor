/**
 * Controlador MVC de la vista de solicitudes de capitanes del administrador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.services.AdminCaptainRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// Admin screen for reviewing captain sign-ups that are still pending approval.
@Controller
@AllArgsConstructor
public class AdminCaptainRequestController {

    private final AdminCaptainRequestService adminCaptainRequestService;

    // Admins only; anyone else is sent to login. The pending requests go to the JSP through the model.
    @GetMapping("/admin-captain-requests")
    public String adminCaptainRequests(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        model.addAttribute("pendingRequests", adminCaptainRequestService.getPendingRequests());
        return "admin-captain-requests";
    }
}

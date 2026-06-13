/**
 * Controlador MVC de la vista de solicitudes de capitanes del administrador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.services.AdminCaptainRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class AdminCaptainRequestController extends MvcSessionController {

    private final AdminCaptainRequestService adminCaptainRequestService;

    @GetMapping("/admin-captain-requests")
    public String adminCaptainRequests(HttpSession session, Model model) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("pendingRequests", adminCaptainRequestService.getPendingRequests());
        return "admin-captain-requests";
    }

    @PostMapping("/admin-captain-requests/{id}/aprobar")
    public String approveRequest(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        Integer adminUserId = currentUserId(session);
        try {
            adminCaptainRequestService.approveRequest(adminUserId, id);
            attr.addFlashAttribute("success", "Solicitud aprobada. El capitán ha sido creado.");
        } catch (NoSuchElementException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captain-requests";
    }

    @PostMapping("/admin-captain-requests/{id}/rechazar")
    public String rejectRequest(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        Integer adminUserId = currentUserId(session);
        try {
            adminCaptainRequestService.rejectRequest(adminUserId, id);
            attr.addFlashAttribute("success", "Solicitud rechazada.");
        } catch (NoSuchElementException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captain-requests";
    }
}

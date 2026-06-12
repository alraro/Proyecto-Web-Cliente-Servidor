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

// Admin screen for reviewing captain sign-ups that are still pending approval.
// Approve/reject are plain POST forms with flash messages, following the SSR pattern of the admin views.
@Controller
@AllArgsConstructor
public class AdminCaptainRequestController {

    private final AdminCaptainRequestService adminCaptainRequestService;

    private boolean notAdmin(HttpSession session) {
        return !"ADMINISTRADOR".equals(session.getAttribute("role"));
    }

    // Admins only; anyone else is sent to login. The pending requests go to the JSP through the model.
    @GetMapping("/admin-captain-requests")
    public String adminCaptainRequests(HttpSession session, Model model) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("pendingRequests", adminCaptainRequestService.getPendingRequests());
        return "admin-captain-requests";
    }

    // Approves a request (creates the captain user) and comes back with a flash message.
    @PostMapping("/admin-captain-requests/{id}/aprobar")
    public String approveRequest(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        Integer adminUserId = (Integer) session.getAttribute("userID");
        try {
            adminCaptainRequestService.approveRequest(adminUserId, id);
            attr.addFlashAttribute("success", "Solicitud aprobada. El capitán ha sido creado.");
        } catch (NoSuchElementException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captain-requests";
    }

    // Rejects a request and comes back with a flash message.
    @PostMapping("/admin-captain-requests/{id}/rechazar")
    public String rejectRequest(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        Integer adminUserId = (Integer) session.getAttribute("userID");
        try {
            adminCaptainRequestService.rejectRequest(adminUserId, id);
            attr.addFlashAttribute("success", "Solicitud rechazada.");
        } catch (NoSuchElementException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captain-requests";
    }
}

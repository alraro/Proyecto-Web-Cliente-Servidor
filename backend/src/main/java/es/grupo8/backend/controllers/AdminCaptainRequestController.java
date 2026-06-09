package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.entity.CaptainRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

/**
 * MVC controller for the admin captain-requests view.
 * Loads pending captain registration requests so the admin can approve or reject them.
 */
@Controller
@AllArgsConstructor
public class AdminCaptainRequestController {

    private final CaptainRequestRepository captainRequestRepository;

    /**
     * Serves the pending captain requests page.
     *
     * @param session HTTP session carrying the user role
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/admin-captain-requests")
    public String adminCaptainRequests(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        List<CaptainRequest> pendingRequests = captainRequestRepository.findByStatus("PENDIENTE");
        model.addAttribute("pendingRequests", pendingRequests);
        return "admin-captain-requests";
    }
}

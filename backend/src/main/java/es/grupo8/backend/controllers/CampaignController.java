package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.CampaignAssignmentService;
import es.grupo8.backend.services.CampaignService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// Controlador MVC para las páginas de gestión de campañas (solo administradores).
// Carga datos desde los servicios para que las JSPs puedan renderizar el contenido inicial con JSTL.
@Controller
@AllArgsConstructor
public class CampaignController {

    private final AdminGuard adminGuard;
    private final CampaignService campaignService;
    private final CampaignAssignmentService campaignAssignmentService;

    // Devuelve true si el usuario en sesión es administrador
    private boolean isAdmin(HttpSession session) {
        Object token = session.getAttribute("token");
        if (token == null) return false;
        return adminGuard.isAdmin("Bearer " + token);
    }

    // Listado principal de campañas
    @GetMapping("/admin-campaigns")
    public String adminCampaigns(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        Page<CampaignDTO> campaigns = campaignService.getCampaigns(
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "startDate"))
        );
        long[] counts = campaignService.getCampaignCounts();
        List<?> campaignTypes = campaignService.getCampaignTypes();

        model.addAttribute("campaigns", campaigns);
        model.addAttribute("campaignTypes", campaignTypes);
        model.addAttribute("counts", counts);
        model.addAttribute("userId", userId);
        return "admin-campaigns";
    }

    // Asignación de coordinadores y capitanes a campañas
    @GetMapping("/admin-campaign-assignments")
    public String adminCampaignAssignments(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-campaign-assignments";
    }

    // Gestión de capitanes por campaña
    @GetMapping("/admin-captains")
    public String adminCaptains(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-captains";
    }

    // Gestión de coordinadores por campaña
    @GetMapping("/admin-coordinators")
    public String adminCoordinators(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-coordinators";
    }
}

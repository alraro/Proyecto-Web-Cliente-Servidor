package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.security.CaptainGuard;
import es.grupo8.backend.services.CaptainDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// Controlador MVC para las páginas del capitán.
// Carga los datos del servidor para el renderizado inicial con JSTL.
@Controller
@AllArgsConstructor
public class CaptainController {

    private final CaptainGuard captainGuard;
    private final CaptainDashboardService captainDashboardService;

    // Devuelve true si el usuario en sesión es capitán
    private boolean isCaptain(HttpSession session) {
        Object token = session.getAttribute("token");
        if (token == null) return false;
        return captainGuard.isUserCaptain("Bearer " + token);
    }

    // Pantalla de bienvenida del capitán
    @GetMapping("/captain")
    public String captain(HttpSession session, Model model) {
        if (!isCaptain(session)) return "redirect:/login";

        model.addAttribute("userName", session.getAttribute("nombre"));
        return "captain";
    }

    // Panel principal del capitán con sus campañas
    @GetMapping("/captain-dashboard")
    public String captainDashboard(HttpSession session, Model model) {
        if (!isCaptain(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = captainDashboardService.getMyCampaigns(userId);
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("campaigns", campaigns);
        return "captain-dashboard";
    }

    // Tiendas del capitán en sus campañas
    @GetMapping("/captain-stores")
    public String captainStores(HttpSession session, Model model) {
        if (!isCaptain(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = captainDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "captain-stores";
    }

    // Incidencias registradas por el capitán
    @GetMapping("/captain-incidents")
    public String captainIncidents(HttpSession session, Model model) {
        if (!isCaptain(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = captainDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "captain-incidents";
    }

    // Control de asistencia de voluntarios
    @GetMapping("/captain-attendance")
    public String captainAttendance(HttpSession session, Model model) {
        if (!isCaptain(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = captainDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "captain-attendance";
    }
}

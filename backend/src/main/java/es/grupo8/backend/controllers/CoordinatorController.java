package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.security.CoordinatorGuard;
import es.grupo8.backend.services.CoordinatorDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// Controlador MVC para las páginas del coordinador.
// Carga los datos necesarios en el modelo para el renderizado inicial con JSTL.
@Controller
@AllArgsConstructor
public class CoordinatorController {

    private final CoordinatorGuard coordinatorGuard;
    private final CoordinatorDashboardService coordinatorDashboardService;

    // Devuelve true si el usuario en sesión es coordinador
    private boolean isCoordinator(HttpSession session) {
        Object token = session.getAttribute("token");
        if (token == null) return false;
        return coordinatorGuard.isCoordinator("Bearer " + token);
    }

    // Pantalla de bienvenida del coordinador
    @GetMapping("/coordinator")
    public String coordinator(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        model.addAttribute("userName", session.getAttribute("nombre"));
        return "coordinator";
    }

    // Panel principal del coordinador con sus campañas
    @GetMapping("/coordinator-dashboard")
    public String coordinatorDashboard(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> myCampaigns = coordinatorDashboardService.getMyCampaigns(userId);
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("myCampaigns", myCampaigns);
        return "coordinator-dashboard";
    }

    // Lista de campañas del coordinador
    @GetMapping("/coordinator-campaigns")
    public String coordinatorCampaigns(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = coordinatorDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "coordinator-campaigns";
    }

    // Tiendas asignadas al coordinador en sus campañas
    @GetMapping("/coordinator-stores")
    public String coordinatorStores(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = coordinatorDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "coordinator-stores";
    }

    // Capitanes asignados en las campañas del coordinador
    @GetMapping("/coordinator-captains")
    public String coordinatorCaptains(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = coordinatorDashboardService.getMyCampaigns(userId);
        List<UserDTO> captains = coordinatorDashboardService.getCaptains(null);
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("captains", captains);
        return "coordinator-captains";
    }

    // Asignación de voluntarios a los turnos del coordinador
    @GetMapping("/coordinator-volunteers")
    public String coordinatorVolunteers(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        List<VoluntarioResponseDto> volunteers = coordinatorDashboardService.getVolunteers();
        List<PartnerEntityResponseDto> partnerEntities = coordinatorDashboardService.getPartnerEntities();
        model.addAttribute("volunteers", volunteers);
        model.addAttribute("partnerEntities", partnerEntities);
        return "coordinator-volunteers";
    }

    // Colaboradores de campaña del coordinador
    @GetMapping("/coordinator-collaborators")
    public String coordinatorCollaborators(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = coordinatorDashboardService.getMyCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "coordinator-collaborators";
    }

    // Entidades colaboradoras con voluntarios asignados en las campañas
    @GetMapping("/coordinator-entities")
    public String coordinatorEntities(HttpSession session, Model model) {
        if (!isCoordinator(session)) return "redirect:/login";

        List<PartnerEntityResponseDto> partnerEntities = coordinatorDashboardService.getPartnerEntities();
        model.addAttribute("partnerEntities", partnerEntities);
        return "coordinator-entities";
    }
}

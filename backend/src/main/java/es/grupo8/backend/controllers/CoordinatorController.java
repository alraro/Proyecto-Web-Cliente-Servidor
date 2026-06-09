package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.services.CoordinatorDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

/**
 * MVC controller for coordinator views.
 * Loads model data from CoordinatorDashboardService for server-side JSP rendering.
 */
@Controller
@AllArgsConstructor
public class CoordinatorController {

    private final CoordinatorDashboardService coordinatorDashboardService;

    /**
     * Serves the coordinator welcome page.
     *
     * @param session HTTP session carrying the user role
     * @return view name or redirect
     */
    @GetMapping("/coordinator")
    public String coordinator(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        return "coordinator";
    }

    /**
     * Serves the coordinator dashboard with the user's assigned campaigns.
     *
     * @param session HTTP session carrying role, userId and nombre
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-dashboard")
    public String coordinatorDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("myCampaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-dashboard";
    }

    /**
     * Serves the coordinator campaigns list.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-campaigns")
    public String coordinatorCampaigns(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-campaigns";
    }

    /**
     * Serves the coordinator stores page with campaign selector.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-stores")
    public String coordinatorStores(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-stores";
    }

    /**
     * Serves the coordinator captains page with campaign selector.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-captains")
    public String coordinatorCaptains(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-captains";
    }

    /**
     * Serves the volunteer assignment page with campaigns, volunteers and partner entities.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-volunteers")
    public String coordinatorVolunteers(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        List<VoluntarioResponseDto> volunteers = coordinatorDashboardService.getVolunteers();
        List<PartnerEntityResponseDto> partnerEntities = coordinatorDashboardService.getPartnerEntities();
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        model.addAttribute("volunteers", volunteers);
        model.addAttribute("partnerEntities", partnerEntities);
        return "coordinator-volunteers";
    }

    /**
     * Serves the collaborators management page.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-collaborators")
    public String coordinatorCollaborators(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-collaborators";
    }

    /**
     * Serves the partner entities page with campaign selector for AJAX filtering.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/coordinator-entities")
    public String coordinatorEntities(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"COORDINADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId));
        return "coordinator-entities";
    }
}

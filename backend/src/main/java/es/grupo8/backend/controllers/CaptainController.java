package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.services.CaptainDashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

/**
 * MVC controller for captain views.
 * Loads model data from CaptainDashboardService for server-side JSP rendering.
 */
@Controller
@AllArgsConstructor
public class CaptainController {

    private final CaptainDashboardService captainDashboardService;

    /**
     * Serves the captain welcome page.
     *
     * @param session HTTP session carrying the user role
     * @return view name or redirect
     */
    @GetMapping("/captain")
    public String captain(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"CAPITAN".equals(role)) {
            return "redirect:/login";
        }
        return "captain";
    }

    /**
     * Serves the captain dashboard with the user's assigned campaigns.
     *
     * @param session HTTP session carrying role, userId and nombre
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/captain-dashboard")
    public String captainDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"CAPITAN".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId));
        return "captain-dashboard";
    }

    /**
     * Serves the captain stores page with campaign selector.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/captain-stores")
    public String captainStores(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"CAPITAN".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId));
        return "captain-stores";
    }

    /**
     * Serves the incidents page with campaign selector.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/captain-incidents")
    public String captainIncidents(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"CAPITAN".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId));
        return "captain-incidents";
    }

    /**
     * Serves the attendance tracking page with campaign selector.
     *
     * @param session HTTP session carrying role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/captain-attendance")
    public String captainAttendance(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"CAPITAN".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId));
        return "captain-attendance";
    }
}

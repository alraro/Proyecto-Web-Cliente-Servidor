/**
 * Controlador MVC de las vistas del capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.services.CaptainDashboardService;
import es.grupo8.backend.services.CaptainShiftService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// MVC controller for the captain views. Selections travel as GET params and the data is
// rendered server-side; the only POST is the incident report form.
@Controller
@AllArgsConstructor
public class CaptainController {

    private final CaptainDashboardService captainDashboardService;
    private final CaptainShiftService captainShiftService;

    private boolean notCaptain(HttpSession session) {
        return !"CAPITAN".equals(session.getAttribute("role"));
    }

    private Integer userId(HttpSession session) {
        return (Integer) session.getAttribute("userID");
    }

    // Captain welcome page.
    @GetMapping("/captain")
    public String captain(HttpSession session) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        return "captain";
    }

    // Dashboard with the captain's campaigns.
    @GetMapping("/captain-dashboard")
    public String captainDashboard(HttpSession session, Model model) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId(session)));
        return "captain-dashboard";
    }

    // Stores page. With ?campaignId= the stores are listed; adding &storeId= also renders
    // that store's volunteer shifts, all server-side.
    @GetMapping("/captain-stores")
    public String captainStores(HttpSession session,
                                @RequestParam(required = false) Integer campaignId,
                                @RequestParam(required = false) Integer storeId,
                                Model model) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", captainDashboardService.getMyStores(userId(session), campaignId));
            if (storeId != null) {
                model.addAttribute("selectedStoreId", storeId);
                model.addAttribute("shifts",
                        captainDashboardService.getVolunteerShifts(userId(session), campaignId, storeId));
            }
        }
        return "captain-stores";
    }

    // Incident page. The campaign/store selection is a GET form; reporting is a POST below.
    @GetMapping("/captain-incidents")
    public String captainIncidents(HttpSession session,
                                   @RequestParam(required = false) Integer campaignId,
                                   @RequestParam(required = false) Integer storeId,
                                   Model model) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", captainDashboardService.getMyStores(userId(session), campaignId));
            if (storeId != null) {
                model.addAttribute("selectedStoreId", storeId);
                model.addAttribute("incidents",
                        captainDashboardService.getIncidents(userId(session), campaignId, storeId));
            }
        }
        return "captain-incidents";
    }

    // Creates the incident and returns to the same campaign/store with a flash message.
    @PostMapping("/captain-incidents/crear")
    public String createIncident(HttpSession session,
                                 @RequestParam Integer campaignId,
                                 @RequestParam Integer storeId,
                                 @RequestParam String description,
                                 RedirectAttributes attr) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        try {
            captainDashboardService.createIncident(userId(session), campaignId, storeId,
                    description != null && !description.trim().isEmpty() ? description.trim() : null);
            attr.addFlashAttribute("success", "Incidencia registrada correctamente.");
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/captain-incidents?campaignId=" + campaignId + "&storeId=" + storeId;
    }

    // Attendance page: with ?campaignId= the whole team's shifts are rendered server-side.
    @GetMapping("/captain-attendance")
    public String captainAttendance(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (notCaptain(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(userId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("teamShifts", captainShiftService.getMyTeamShifts(userId(session), campaignId));
        }
        return "captain-attendance";
    }
}

/**
 * Controlador MVC de las vistas del capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
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

@Controller
@AllArgsConstructor
public class CaptainController extends MvcSessionController {

    private final CaptainDashboardService captainDashboardService;
    private final CaptainShiftService captainShiftService;

    @GetMapping("/captain")
    public String captain(HttpSession session) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        return "captain";
    }

    @GetMapping("/captain-dashboard")
    public String captainDashboard(HttpSession session, Model model) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(currentUserId(session)));
        return "captain-dashboard";
    }

    @GetMapping("/captain-stores")
    public String captainStores(HttpSession session,
                                @RequestParam(required = false) Integer campaignId,
                                @RequestParam(required = false) Integer storeId,
                                Model model) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", captainDashboardService.getMyStores(currentUserId(session), campaignId));
            if (storeId != null) {
                model.addAttribute("selectedStoreId", storeId);
                model.addAttribute("shifts",
                        captainDashboardService.getVolunteerShifts(currentUserId(session), campaignId, storeId));
            }
        }
        return "captain-stores";
    }

    @GetMapping("/captain-incidents")
    public String captainIncidents(HttpSession session,
                                   @RequestParam(required = false) Integer campaignId,
                                   @RequestParam(required = false) Integer storeId,
                                   Model model) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", captainDashboardService.getMyStores(currentUserId(session), campaignId));
            if (storeId != null) {
                model.addAttribute("selectedStoreId", storeId);
                model.addAttribute("incidents",
                        captainDashboardService.getIncidents(currentUserId(session), campaignId, storeId));
            }
        }
        return "captain-incidents";
    }

    @PostMapping("/captain-incidents/crear")
    public String createIncident(HttpSession session,
                                 @RequestParam Integer campaignId,
                                 @RequestParam Integer storeId,
                                 @RequestParam String description,
                                 RedirectAttributes attr) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        try {
            captainDashboardService.createIncident(currentUserId(session), campaignId, storeId,
                    description != null && !description.trim().isEmpty() ? description.trim() : null);
            attr.addFlashAttribute("success", "Incidencia registrada correctamente.");
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/captain-incidents?campaignId=" + campaignId + "&storeId=" + storeId;
    }

    @GetMapping("/captain-attendance")
    public String captainAttendance(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (!hasRole(session, "CAPITAN")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", captainDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("teamShifts", captainShiftService.getMyTeamShifts(currentUserId(session), campaignId));
        }
        return "captain-attendance";
    }
}

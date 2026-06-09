package es.grupo8.backend.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.services.CampaignAssignmentService;
import es.grupo8.backend.services.CampaignService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

/**
 * MVC controller for admin campaign-management views.
 * Loads model data from services so JSPs can render the initial page server-side.
 */
@Controller
@AllArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignAssignmentService campaignAssignmentService;

    /**
     * Serves the campaign list page with a paginated campaign list and type options.
     *
     * @param session HTTP session carrying the user role
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/admin-campaigns")
    public String adminCampaigns(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        Page<CampaignDTO> campaigns = campaignService.getCampaigns(
                null, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("campaignTypes", campaignService.getCampaignTypes());
        model.addAttribute("counts", campaignService.getCampaignCounts());
        return "admin-campaigns";
    }

    /**
     * Serves the campaign-assignment page with a flat list of all campaigns.
     *
     * @param session HTTP session carrying the user role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/admin-campaign-assignments")
    public String adminCampaignAssignments(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-campaign-assignments";
    }

    /**
     * Serves the captains-management page with the full campaign list.
     *
     * @param session HTTP session carrying the user role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/admin-captains")
    public String adminCaptains(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-captains";
    }

    /**
     * Serves the coordinators-management page with the full campaign list.
     *
     * @param session HTTP session carrying the user role and userId
     * @param model   Spring MVC model for the JSP
     * @return view name or redirect
     */
    @GetMapping("/admin-coordinators")
    public String adminCoordinators(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        Integer userId = (Integer) session.getAttribute("userID");
        List<CampaignDTO> campaigns = campaignAssignmentService.getCampaigns(userId);
        model.addAttribute("campaigns", campaigns);
        return "admin-coordinators";
    }
}

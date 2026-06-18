/**
 * Controlador MVC de las vistas del coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 65%
 * - Alejandro Calvo Aguilar: 15%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.ShiftService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class CoordinatorController extends MvcSessionController {

    private final CoordinatorDashboardService coordinatorDashboardService;
    private final ShiftService shiftService;

    @GetMapping("/coordinator")
    public String coordinator(HttpSession session) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        return "coordinator";
    }

    @GetMapping("/coordinator-dashboard")
    public String coordinatorDashboard(HttpSession session, Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("myCampaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        return "coordinator-dashboard";
    }

    @GetMapping("/coordinator-campaigns")
    public String coordinatorCampaigns(HttpSession session, Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        return "coordinator-campaigns";
    }

    @GetMapping("/coordinator-stores")
    public String coordinatorStores(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", coordinatorDashboardService.getMyStores(campaignId));
        }
        return "coordinator-stores";
    }

    @GetMapping("/coordinator-captains")
    public String coordinatorCaptains(HttpSession session,
                                      @RequestParam(required = false) Integer campaignId,
                                      Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("captains", coordinatorDashboardService.getCaptains(campaignId));
        }
        return "coordinator-captains";
    }

    @PostMapping("/coordinator-captains/registrar")
    public String registerCaptain(HttpSession session,
                                  @RequestParam Integer campaignId,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  RedirectAttributes attr) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        try {
            coordinatorDashboardService.registerCaptain(currentUserId(session), name, email, password, campaignId);
            attr.addFlashAttribute("success", "Capitán registrado. Pendiente de aprobación del administrador.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-captains?campaignId=" + campaignId;
    }

    @GetMapping("/coordinator-volunteers")
    public String coordinatorVolunteers(HttpSession session,
                                        @RequestParam(required = false) Integer campaignId,
                                        @RequestParam(required = false) Integer storeId,
                                        @RequestParam(required = false) Integer shiftId,
                                        Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        model.addAttribute("volunteers", coordinatorDashboardService.getVolunteers());
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", shiftService.getStoresForCampaign(campaignId));
            if (storeId != null) {
                model.addAttribute("selectedStoreId", storeId);
                model.addAttribute("shifts", shiftService.getShifts(campaignId, storeId));
                if (shiftId != null) {
                    model.addAttribute("selectedShiftId", shiftId);
                }
            }
        }
        return "coordinator-volunteers";
    }

    @PostMapping("/coordinator-volunteers/asignar")
    public String assignVolunteerShift(HttpSession session,
                                       @RequestParam Integer campaignId,
                                       @RequestParam Integer storeId,
                                       @RequestParam Integer volunteerId,
                                       @RequestParam String shiftDay,
                                       @RequestParam String startTime,
                                       @RequestParam String endTime,
                                       RedirectAttributes attr) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        try {
            coordinatorDashboardService.assignVolunteerShift(currentUserId(session), volunteerId,
                    campaignId, storeId, shiftDay, startTime, endTime);
            attr.addFlashAttribute("success", "Voluntario asignado correctamente.");
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-volunteers?campaignId=" + campaignId + "&storeId=" + storeId;
    }

    @GetMapping("/coordinator-collaborators")
    public String coordinatorCollaborators(HttpSession session,
                                           @RequestParam(required = false) Integer crear,
                                           @RequestParam(required = false) Integer editar,
                                           Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("volunteers", coordinatorDashboardService.getVolunteers());
        model.addAttribute("partnerEntities", coordinatorDashboardService.getPartnerEntities());
        if (crear != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("isCreating", true);
        } else if (editar != null) {
            for (VoluntarioResponseDto v : coordinatorDashboardService.getVolunteers()) {
                if (editar.equals(v.id())) {
                    model.addAttribute("editEntity", v);
                    model.addAttribute("showForm", true);
                    break;
                }
            }
        }
        return "coordinator-collaborators";
    }

    @PostMapping("/coordinator-collaborators/guardar")
    public String saveCollaborator(HttpSession session,
                                   @RequestParam(required = false) Integer id,
                                   @RequestParam String name,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String address,
                                   @RequestParam(required = false) Integer partnerEntityId,
                                   RedirectAttributes attr) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        try {
            if (id == null) {
                coordinatorDashboardService.createVolunteer(currentUserId(session), emptyToNull(name),
                        emptyToNull(phone), emptyToNull(email), emptyToNull(address), partnerEntityId);
                attr.addFlashAttribute("success", "Colaborador creado correctamente.");
            } else {
                coordinatorDashboardService.updateVolunteer(currentUserId(session), id, emptyToNull(name),
                        emptyToNull(phone), emptyToNull(email), emptyToNull(address), partnerEntityId);
                attr.addFlashAttribute("success", "Colaborador actualizado correctamente.");
            }
        } catch (IllegalArgumentException | NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-collaborators";
    }

    @GetMapping("/coordinator-entities")
    public String coordinatorEntities(HttpSession session,
                                      @RequestParam(required = false) Integer campaignId,
                                      Model model) {
        if (!hasRole(session, "COORDINADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(currentUserId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("entities", coordinatorDashboardService.getCampaignEntities(campaignId));
        }
        return "coordinator-entities";
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}

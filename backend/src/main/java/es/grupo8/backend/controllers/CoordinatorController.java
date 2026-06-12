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

// MVC controller for the coordinator views. Selections travel as GET params, the data is
// rendered server-side and the actions (register captain, save collaborator, assign shift)
// are plain POST forms with flash messages.
@Controller
@AllArgsConstructor
public class CoordinatorController {

    private final CoordinatorDashboardService coordinatorDashboardService;
    private final ShiftService shiftService;

    private boolean notCoordinator(HttpSession session) {
        return !"COORDINADOR".equals(session.getAttribute("role"));
    }

    private Integer userId(HttpSession session) {
        return (Integer) session.getAttribute("userID");
    }

    // Coordinator welcome page.
    @GetMapping("/coordinator")
    public String coordinator(HttpSession session) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        return "coordinator";
    }

    // Dashboard with the coordinator's campaigns.
    @GetMapping("/coordinator-dashboard")
    public String coordinatorDashboard(HttpSession session, Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("userName", session.getAttribute("nombre"));
        model.addAttribute("myCampaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
        return "coordinator-dashboard";
    }

    // Read-only list of the coordinator's campaigns.
    @GetMapping("/coordinator-campaigns")
    public String coordinatorCampaigns(HttpSession session, Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
        return "coordinator-campaigns";
    }

    // Stores page: with ?campaignId= the campaign's stores are rendered server-side.
    @GetMapping("/coordinator-stores")
    public String coordinatorStores(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("stores", coordinatorDashboardService.getMyStores(campaignId));
        }
        return "coordinator-stores";
    }

    // Captains page: with ?campaignId= the campaign's captains are listed and the
    // register form appears. Registration is the POST below.
    @GetMapping("/coordinator-captains")
    public String coordinatorCaptains(HttpSession session,
                                      @RequestParam(required = false) Integer campaignId,
                                      Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);
            model.addAttribute("captains", coordinatorDashboardService.getCaptains(campaignId));
        }
        return "coordinator-captains";
    }

    // Sends a captain registration request (pending admin approval) and comes back with a flash.
    @PostMapping("/coordinator-captains/registrar")
    public String registerCaptain(HttpSession session,
                                  @RequestParam Integer campaignId,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  RedirectAttributes attr) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        try {
            coordinatorDashboardService.registerCaptain(userId(session), name, email, password, campaignId);
            attr.addFlashAttribute("success", "Capitán registrado. Pendiente de aprobación del administrador.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-captains?campaignId=" + campaignId;
    }

    // Shift-assignment page. campaignId/storeId narrow the shift list and shiftId opens
    // the assignment form for that shift, all rendered server-side.
    @GetMapping("/coordinator-volunteers")
    public String coordinatorVolunteers(HttpSession session,
                                        @RequestParam(required = false) Integer campaignId,
                                        @RequestParam(required = false) Integer storeId,
                                        @RequestParam(required = false) Integer shiftId,
                                        Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
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

    // Assigns a volunteer to a shift and returns to the same selection with a flash.
    @PostMapping("/coordinator-volunteers/asignar")
    public String assignVolunteerShift(HttpSession session,
                                       @RequestParam Integer campaignId,
                                       @RequestParam Integer storeId,
                                       @RequestParam Integer volunteerId,
                                       @RequestParam String shiftDay,
                                       @RequestParam String startTime,
                                       @RequestParam String endTime,
                                       RedirectAttributes attr) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        try {
            coordinatorDashboardService.assignVolunteerShift(userId(session), volunteerId,
                    campaignId, storeId, shiftDay, startTime, endTime);
            attr.addFlashAttribute("success", "Voluntario asignado correctamente.");
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-volunteers?campaignId=" + campaignId + "&storeId=" + storeId;
    }

    // Collaborators page with the create/edit form driven by ?crear=1 / ?editar=id,
    // the same pattern the other admin views follow.
    @GetMapping("/coordinator-collaborators")
    public String coordinatorCollaborators(HttpSession session,
                                           @RequestParam(required = false) Integer crear,
                                           @RequestParam(required = false) Integer editar,
                                           Model model) {
        if (notCoordinator(session)) {
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

    // Creates or updates a collaborator and comes back with a flash message.
    @PostMapping("/coordinator-collaborators/guardar")
    public String saveCollaborator(HttpSession session,
                                   @RequestParam(required = false) Integer id,
                                   @RequestParam String name,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String address,
                                   @RequestParam(required = false) Integer partnerEntityId,
                                   RedirectAttributes attr) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        try {
            if (id == null) {
                coordinatorDashboardService.createVolunteer(userId(session), emptyToNull(name),
                        emptyToNull(phone), emptyToNull(email), emptyToNull(address), partnerEntityId);
                attr.addFlashAttribute("success", "Colaborador creado correctamente.");
            } else {
                coordinatorDashboardService.updateVolunteer(userId(session), id, emptyToNull(name),
                        emptyToNull(phone), emptyToNull(email), emptyToNull(address), partnerEntityId);
                attr.addFlashAttribute("success", "Colaborador actualizado correctamente.");
            }
        } catch (IllegalArgumentException | NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator-collaborators";
    }

    // Partner entities page: with ?campaignId= the entities with volunteers are rendered.
    @GetMapping("/coordinator-entities")
    public String coordinatorEntities(HttpSession session,
                                      @RequestParam(required = false) Integer campaignId,
                                      Model model) {
        if (notCoordinator(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", coordinatorDashboardService.getMyCampaigns(userId(session)));
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

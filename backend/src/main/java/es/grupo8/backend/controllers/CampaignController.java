/**
 * Controlador MVC de las vistas de gestión de campañas (admin).
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignRequestDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.services.CampaignAssignmentService;
import es.grupo8.backend.services.CampaignService;
import es.grupo8.backend.services.CampaignStoreService;
import es.grupo8.backend.services.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

// MVC controller for the admin campaign views. Everything renders server-side: the JSPs get
// their data through the model and the actions are plain POST forms with flash messages.
@Controller
@AllArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignAssignmentService campaignAssignmentService;
    private final CampaignStoreService campaignStoreService;
    private final StoreService storeService;

    private boolean notAdmin(HttpSession session) {
        return !"ADMINISTRADOR".equals(session.getAttribute("role"));
    }

    private Integer adminId(HttpSession session) {
        return (Integer) session.getAttribute("userID");
    }

    // ── Campaign CRUD ─────────────────────────────────────────────────────────

    // Campaign list page. ?crear=1 opens the empty form and ?editar=id opens it pre-filled,
    // including the store checkboxes, all rendered server-side.
    @GetMapping("/admin-campaigns")
    public String adminCampaigns(HttpSession session,
                                 @RequestParam(required = false) Integer crear,
                                 @RequestParam(required = false) Integer editar,
                                 Model model) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        Page<CampaignDTO> campaigns = campaignService.getCampaigns(
                null, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("campaigns", campaigns.getContent());
        model.addAttribute("campaignTypes", campaignService.getCampaignTypes());

        if (crear != null || editar != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("allStores",
                    storeService.findAll(null, null, null, 0, 100).content());
            if (crear != null) {
                model.addAttribute("isCreating", true);
                model.addAttribute("assignedStoreIds", Set.of());
            } else {
                campaignService.getCampaignById(editar).ifPresent(dto -> {
                    model.addAttribute("editEntity", dto);
                    model.addAttribute("assignedStoreIds", assignedStoreIds(session, editar));
                });
            }
        }
        return "admin-campaigns";
    }

    // Creates or updates a campaign together with its store set, then comes back with a flash.
    @PostMapping("/admin-campaigns/guardar")
    public String saveCampaign(HttpSession session,
                               @RequestParam(required = false) Integer id,
                               @RequestParam String name,
                               @RequestParam Integer typeId,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(required = false) List<Integer> storeIds,
                               RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            CampaignRequestDto dto = new CampaignRequestDto();
            dto.setName(name);
            dto.setTypeId(typeId);
            dto.setStartDate(LocalDate.parse(startDate));
            dto.setEndDate(LocalDate.parse(endDate));

            Integer campaignId;
            if (id == null) {
                campaignId = campaignService.createCampaign(adminId(session), dto).getId();
                attr.addFlashAttribute("success", "Campaña creada correctamente.");
            } else {
                campaignId = campaignService.updateCampaign(adminId(session), id, dto).getId();
                attr.addFlashAttribute("success", "Campaña actualizada correctamente.");
            }
            campaignStoreService.replaceCampaignStores(adminId(session), campaignId,
                    storeIds != null ? storeIds : List.of());
        } catch (DateTimeParseException e) {
            attr.addFlashAttribute("error", "Formato de fecha inválido.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaigns";
    }

    // Deletes a campaign and its assignments.
    @PostMapping("/admin-campaigns/eliminar/{id}")
    public String deleteCampaign(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            campaignService.deleteCampaign(adminId(session), id);
            attr.addFlashAttribute("success", "Campaña eliminada correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaigns";
    }

    // ── Combined assignments page ─────────────────────────────────────────────

    // Coordinator and captain assignments side by side for one campaign.
    @GetMapping("/admin-campaign-assignments")
    public String adminCampaignAssignments(HttpSession session,
                                           @RequestParam(required = false) Integer campaignId,
                                           Model model) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(adminId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                var assignments = campaignAssignmentService.getCampaignAssignments(adminId(session), campaignId);
                model.addAttribute("assignedCoordinators", assignments.getCoordinators());
                model.addAttribute("assignedCaptains", assignments.getCaptains());
                model.addAttribute("availableCoordinators",
                        campaignAssignmentService.getAvailableUsers(adminId(session), campaignId, "COORDINATOR"));
                model.addAttribute("availableCaptains",
                        campaignAssignmentService.getAvailableUsers(adminId(session), campaignId, "CAPTAIN"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-campaign-assignments";
    }

    // Assigns or removes either role from the combined page and returns to the same campaign.
    @PostMapping("/admin-campaign-assignments/asignar")
    public String assignFromCombined(HttpSession session,
                                     @RequestParam Integer campaignId,
                                     @RequestParam Integer userId,
                                     @RequestParam String rol,
                                     RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if ("COORDINATOR".equals(rol)) {
                campaignAssignmentService.assignCoordinator(adminId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Coordinador asignado correctamente.");
            } else {
                campaignAssignmentService.assignCaptain(adminId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Capitán asignado correctamente.");
            }
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaign-assignments?campaignId=" + campaignId;
    }

    @PostMapping("/admin-campaign-assignments/eliminar")
    public String unassignFromCombined(HttpSession session,
                                       @RequestParam Integer campaignId,
                                       @RequestParam Integer userId,
                                       @RequestParam String rol,
                                       RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if ("COORDINATOR".equals(rol)) {
                campaignAssignmentService.unassignCoordinator(adminId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Coordinador desasignado correctamente.");
            } else {
                campaignAssignmentService.unassignCaptain(adminId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Capitán desasignado correctamente.");
            }
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaign-assignments?campaignId=" + campaignId;
    }

    // ── Captains page ─────────────────────────────────────────────────────────

    // Captains page. When a campaign is picked (?campaignId=) the assigned and available
    // captains are loaded into the model so the JSP renders everything server-side.
    @GetMapping("/admin-captains")
    public String adminCaptains(HttpSession session,
                                @RequestParam(required = false) Integer campaignId,
                                Model model) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(adminId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                model.addAttribute("assignedCaptains",
                        campaignAssignmentService.getCampaignAssignments(adminId(session), campaignId).getCaptains());
                model.addAttribute("availableCaptains",
                        campaignAssignmentService.getAvailableUsers(adminId(session), campaignId, "CAPTAIN"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-captains";
    }

    // Assigns a captain and returns to the same campaign with a flash message.
    @PostMapping("/admin-captains/asignar")
    public String assignCaptain(HttpSession session,
                                @RequestParam Integer campaignId,
                                @RequestParam Integer userId,
                                RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.assignCaptain(adminId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Capitán asignado correctamente.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captains?campaignId=" + campaignId;
    }

    // Removes a captain from the campaign.
    @PostMapping("/admin-captains/eliminar")
    public String unassignCaptain(HttpSession session,
                                  @RequestParam Integer campaignId,
                                  @RequestParam Integer userId,
                                  RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.unassignCaptain(adminId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Capitán desasignado correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captains?campaignId=" + campaignId;
    }

    // ── Coordinators page ─────────────────────────────────────────────────────

    // Coordinators page, same server-side pattern as the captains one.
    @GetMapping("/admin-coordinators")
    public String adminCoordinators(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(adminId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                model.addAttribute("assignedCoordinators",
                        campaignAssignmentService.getCampaignAssignments(adminId(session), campaignId).getCoordinators());
                model.addAttribute("availableCoordinators",
                        campaignAssignmentService.getAvailableUsers(adminId(session), campaignId, "COORDINATOR"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-coordinators";
    }

    // Assigns a coordinator and returns to the same campaign with a flash message.
    @PostMapping("/admin-coordinators/asignar")
    public String assignCoordinator(HttpSession session,
                                    @RequestParam Integer campaignId,
                                    @RequestParam Integer userId,
                                    RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.assignCoordinator(adminId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Coordinador asignado correctamente.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-coordinators?campaignId=" + campaignId;
    }

    // Removes a coordinator from the campaign.
    @PostMapping("/admin-coordinators/eliminar")
    public String unassignCoordinator(HttpSession session,
                                      @RequestParam Integer campaignId,
                                      @RequestParam Integer userId,
                                      RedirectAttributes attr) {
        if (notAdmin(session)) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.unassignCoordinator(adminId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Coordinador desasignado correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-coordinators?campaignId=" + campaignId;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Ids of the stores currently assigned to the campaign, for pre-checking the form checkboxes.
    private Set<Integer> assignedStoreIds(HttpSession session, Integer campaignId) {
        Set<Integer> ids = new HashSet<>();
        try {
            Map<String, Object> data = campaignStoreService.getCampaignStores(adminId(session), campaignId);
            Object stores = data.get("stores");
            if (stores instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof StoreResponseDto dto) {
                        ids.add(dto.id());
                    }
                }
            }
        } catch (NoSuchElementException ignored) {
        }
        return ids;
    }
}

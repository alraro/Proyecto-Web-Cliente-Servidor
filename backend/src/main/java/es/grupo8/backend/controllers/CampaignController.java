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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

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
import es.grupo8.backend.services.ChainService;
import es.grupo8.backend.services.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class CampaignController extends MvcSessionController {

    private final CampaignService campaignService;
    private final CampaignAssignmentService campaignAssignmentService;
    private final CampaignStoreService campaignStoreService;
    private final StoreService storeService;
    private final ChainService chainService;

    // crear=1 abre el formulario vacio y editar=id lo abre relleno; los filtros acotan la lista de tiendas
    @GetMapping("/admin-campaigns")
    public String adminCampaigns(HttpSession session,
                                 @RequestParam(required = false) Integer crear,
                                 @RequestParam(required = false) Integer editar,
                                 @RequestParam(required = false) Integer chainId,
                                 @RequestParam(required = false) Integer zoneId,
                                 @RequestParam(required = false) Integer localityId,
                                 Model model) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        Page<CampaignDTO> campaigns = campaignService.getCampaigns(
                null, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("campaigns", campaigns.getContent());
        model.addAttribute("campaignTypes", campaignService.getCampaignTypes());

        if (crear != null || editar != null) {
            model.addAttribute("showForm", true);

            List<StoreResponseDto> unfiltered = storeService.findAll(null, null, null, 0, 100).content();
            boolean filtering = chainId != null || zoneId != null || localityId != null;
            model.addAttribute("allStores", filtering
                    ? storeService.findAll(chainId, localityId, zoneId, 0, 100).content()
                    : unfiltered);
            model.addAttribute("chains", chainService.findAll());
            model.addAttribute("zoneOptions", options(unfiltered, true));
            model.addAttribute("localityOptions", options(unfiltered, false));
            model.addAttribute("selectedChainId", chainId);
            model.addAttribute("selectedZoneId", zoneId);
            model.addAttribute("selectedLocalityId", localityId);

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

    @PostMapping("/admin-campaigns/guardar")
    public String saveCampaign(HttpSession session,
                               @RequestParam(required = false) Integer id,
                               @RequestParam String name,
                               @RequestParam Integer typeId,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(required = false) List<Integer> storeIds,
                               RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
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
                campaignId = campaignService.createCampaign(currentUserId(session), dto).getId();
                attr.addFlashAttribute("success", "Campaña creada correctamente.");
            } else {
                campaignId = campaignService.updateCampaign(currentUserId(session), id, dto).getId();
                attr.addFlashAttribute("success", "Campaña actualizada correctamente.");
            }
            campaignStoreService.replaceCampaignStores(currentUserId(session), campaignId,
                    storeIds != null ? storeIds : List.of());
        } catch (DateTimeParseException e) {
            attr.addFlashAttribute("error", "Formato de fecha inválido.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaigns";
    }

    @PostMapping("/admin-campaigns/eliminar/{id}")
    public String deleteCampaign(HttpSession session, @PathVariable Integer id, RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            campaignService.deleteCampaign(currentUserId(session), id);
            attr.addFlashAttribute("success", "Campaña eliminada correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaigns";
    }

    @GetMapping("/admin-campaign-assignments")
    public String adminCampaignAssignments(HttpSession session,
                                           @RequestParam(required = false) Integer campaignId,
                                           Model model) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(currentUserId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                var assignments = campaignAssignmentService.getCampaignAssignments(currentUserId(session), campaignId);
                model.addAttribute("assignedCoordinators", assignments.getCoordinators());
                model.addAttribute("assignedCaptains", assignments.getCaptains());
                model.addAttribute("availableCoordinators",
                        campaignAssignmentService.getAvailableUsers(currentUserId(session), campaignId, "COORDINATOR"));
                model.addAttribute("availableCaptains",
                        campaignAssignmentService.getAvailableUsers(currentUserId(session), campaignId, "CAPTAIN"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-campaign-assignments";
    }

    @PostMapping("/admin-campaign-assignments/asignar")
    public String assignFromCombined(HttpSession session,
                                     @RequestParam Integer campaignId,
                                     @RequestParam Integer userId,
                                     @RequestParam String rol,
                                     RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            if ("COORDINATOR".equals(rol)) {
                campaignAssignmentService.assignCoordinator(currentUserId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Coordinador asignado correctamente.");
            } else {
                campaignAssignmentService.assignCaptain(currentUserId(session), campaignId, userId);
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
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            if ("COORDINATOR".equals(rol)) {
                campaignAssignmentService.unassignCoordinator(currentUserId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Coordinador desasignado correctamente.");
            } else {
                campaignAssignmentService.unassignCaptain(currentUserId(session), campaignId, userId);
                attr.addFlashAttribute("success", "Capitán desasignado correctamente.");
            }
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-campaign-assignments?campaignId=" + campaignId;
    }

    @GetMapping("/admin-captains")
    public String adminCaptains(HttpSession session,
                                @RequestParam(required = false) Integer campaignId,
                                Model model) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(currentUserId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                model.addAttribute("assignedCaptains",
                        campaignAssignmentService.getCampaignAssignments(currentUserId(session), campaignId).getCaptains());
                model.addAttribute("availableCaptains",
                        campaignAssignmentService.getAvailableUsers(currentUserId(session), campaignId, "CAPTAIN"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-captains";
    }

    @PostMapping("/admin-captains/asignar")
    public String assignCaptain(HttpSession session,
                                @RequestParam Integer campaignId,
                                @RequestParam Integer userId,
                                RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.assignCaptain(currentUserId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Capitán asignado correctamente.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captains?campaignId=" + campaignId;
    }

    @PostMapping("/admin-captains/eliminar")
    public String unassignCaptain(HttpSession session,
                                  @RequestParam Integer campaignId,
                                  @RequestParam Integer userId,
                                  RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.unassignCaptain(currentUserId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Capitán desasignado correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-captains?campaignId=" + campaignId;
    }

    @GetMapping("/admin-coordinators")
    public String adminCoordinators(HttpSession session,
                                    @RequestParam(required = false) Integer campaignId,
                                    Model model) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignAssignmentService.getCampaigns(currentUserId(session)));
        if (campaignId != null) {
            try {
                model.addAttribute("selectedCampaignId", campaignId);
                model.addAttribute("assignedCoordinators",
                        campaignAssignmentService.getCampaignAssignments(currentUserId(session), campaignId).getCoordinators());
                model.addAttribute("availableCoordinators",
                        campaignAssignmentService.getAvailableUsers(currentUserId(session), campaignId, "COORDINATOR"));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "admin-coordinators";
    }

    @PostMapping("/admin-coordinators/asignar")
    public String assignCoordinator(HttpSession session,
                                    @RequestParam Integer campaignId,
                                    @RequestParam Integer userId,
                                    RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.assignCoordinator(currentUserId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Coordinador asignado correctamente.");
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-coordinators?campaignId=" + campaignId;
    }

    @PostMapping("/admin-coordinators/eliminar")
    public String unassignCoordinator(HttpSession session,
                                      @RequestParam Integer campaignId,
                                      @RequestParam Integer userId,
                                      RedirectAttributes attr) {
        if (!hasRole(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        try {
            campaignAssignmentService.unassignCoordinator(currentUserId(session), campaignId, userId);
            attr.addFlashAttribute("success", "Coordinador desasignado correctamente.");
        } catch (NoSuchElementException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-coordinators?campaignId=" + campaignId;
    }

    // Opciones de zona/localidad para los filtros, sacadas de las propias tiendas
    private Map<Integer, String> options(List<StoreResponseDto> stores, boolean zones) {
        Map<String, Integer> byName = new TreeMap<>();
        for (StoreResponseDto s : stores) {
            Integer id = zones ? s.zoneId() : s.localityId();
            String name = zones ? s.zone() : s.locality();
            if (id != null && name != null) {
                byName.put(name, id);
            }
        }
        Map<Integer, String> result = new LinkedHashMap<>();
        byName.forEach((name, id) -> result.put(id, name));
        return result;
    }

    // Ids de las tiendas ya asignadas, para marcar los checkbox al editar
    private Set<Integer> assignedStoreIds(HttpSession session, Integer campaignId) {
        Set<Integer> ids = new HashSet<>();
        try {
            Map<String, Object> data = campaignStoreService.getCampaignStores(currentUserId(session), campaignId);
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

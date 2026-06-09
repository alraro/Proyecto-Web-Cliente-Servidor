/*
* Autores:
*  - Hugo Herrero González: 80%
*  - Alfonso Ramos Rojas: 20%
*
*/

package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.dto.AdminDTO;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.services.AdminService;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.PartnerEntityService;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PartnerEntityService partnerEntityService;

    @GetMapping("/admin-coordinators")
    public String adminCoordinators(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-coordinators";
    }

    @GetMapping("/admin-captains")
    public String adminCaptains(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-captains";
    }

    @GetMapping("/admin-campaigns")
    public String adminCampaigns(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-campaigns";
    }

    @GetMapping("/campaigns")
    public String campaigns(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "campaigns";
    }

    @GetMapping("/admin")
    public String backToMenu(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin";
    }

    @GetMapping("/admin-dashboard")
    public String dashboard(@RequestParam(value = "campaignId", required = false) Integer campaignId,
                            HttpSession session,
                            Model model) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        model.addAttribute("campaignsList", adminService.getAllCampaigns());

        if (campaignId != null) {
            model.addAttribute("selectedCampaignId", campaignId);

            List<AdminDTO> chainData = adminService.getChainCoverage(campaignId);
            List<AdminDTO> localityData = adminService.getLocalityCoverage(campaignId);
            List<AdminDTO> zoneData = adminService.getZoneCoverage(campaignId);

            model.addAttribute("chainData", chainData);
            model.addAttribute("localityData", localityData);
            model.addAttribute("zoneData", zoneData);

            long totalStores = chainData.stream().mapToLong(AdminDTO::getStoresInCampaign).sum();
            long chainsActive = chainData.stream().filter(c -> c.getStoresInCampaign() > 0).count();
            long zonesActive = zoneData.stream().filter(z -> z.getStoresInCampaign() > 0).count();

            model.addAttribute("kpiStores", totalStores);
            model.addAttribute("kpiChains", chainsActive);
            model.addAttribute("kpiZones", zonesActive);
            model.addAttribute("kpiStatus", "Activa (ID: " + campaignId + ")");
        }

        return "admin-dashboard";
    }

    @GetMapping("/admin-createusers")
    public String adminCreateUsers(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        return "admin-createusers";
    }

    @PostMapping("/admin-createusers")
    public String submitCreateUser(@RequestParam(value = "nombre") String nombre,
                                   @RequestParam(value = "email") String email,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "confirmPassword") String confirmPassword,
                                   @RequestParam(value = "telefono", required = false) String telefono,
                                   @RequestParam(value = "domicilio", required = false) String domicilio,
                                   @RequestParam(value = "cp", required = false) String cp,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/admin-createusers";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/admin-createusers";
        }

        if (telefono != null && telefono.isBlank()) telefono = null;
        if (domicilio != null && domicilio.isBlank()) domicilio = null;
        if (cp != null && cp.isBlank()) cp = null;

        try {
            authService.register(nombre, email, password, telefono, domicilio, cp);
            redirectAttributes.addFlashAttribute("success", "Usuario " + nombre + " creado, esperando validación de rol");
            return "redirect:/admin-createusers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el usuario.");
            return "redirect:/admin-createusers";
        }
    }

    @GetMapping("/admin-chains")
    public String adminChains(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-chains";
    }

    @GetMapping("/admin-stores")
    public String adminStores(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-stores";
    }

    @GetMapping("/admin-validate-users")
    public String adminValidateUsers(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-validate-users";
    }

    @GetMapping("/admin-users")
    public String adminUsers(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        return "admin-users";
    }


    @GetMapping("/admin-partner-entities")
    public String adminPartnerEntities(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer crear,
            @RequestParam(required = false) Integer editar,
            Model model) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        PaginatedResponse<PartnerEntityResponseDto> response =
                partnerEntityService.getAllPartnerEntities(page, size, sort, search);

        model.addAttribute("entities", response.content());
        model.addAttribute("currentPage", response.page());
        model.addAttribute("totalPages", response.totalPages());
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSize", size);

        String sortField = "id";
        String sortOrder = "asc";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            sortOrder = parts.length > 1 ? parts[1] : "asc";
        }
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortOrder", sortOrder);

        if (crear != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("isCreating", true);
        } else if (editar != null) {
            try {
                PartnerEntityResponseDto entity = partnerEntityService.getPartnerEntityById(editar);
                model.addAttribute("editEntity", entity);
                model.addAttribute("showForm", true);
            } catch (RuntimeException ignored) {
            }
        }

        return "admin-partner-entities";
    }

    @PostMapping("/admin-partner-entities/guardar")
    public String savePartnerEntity(
            HttpSession session,
            @RequestParam(required = false) Integer id,
            @RequestParam String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String telefono,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        PartnerEntityRequestDto dto = new PartnerEntityRequestDto();
        dto.setName(nombre);
        dto.setAddress(direccion);
        dto.setPhone(telefono);

        try {
            if (id == null) {
                partnerEntityService.createPartnerEntity(dto);
                attr.addFlashAttribute("success", "Entidad creada correctamente.");
            } else {
                partnerEntityService.updatePartnerEntity(id, dto);
                attr.addFlashAttribute("success", "Entidad actualizada correctamente.");
            }
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-partner-entities";
    }

    @PostMapping("/admin-partner-entities/eliminar/{id}")
    public String deletePartnerEntity(
            HttpSession session,
            @PathVariable Integer id,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        try {
            partnerEntityService.deletePartnerEntity(id);
            attr.addFlashAttribute("success", "Entidad eliminada.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-partner-entities";
    }

    @GetMapping("/admin-incidents")
    public String adminIncidents(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if(!"ADMINISTRADOR".equals(role)){
            return "redirect:/login";
        }

        List<Map<String, Object>> incidents = adminService.getAllIncidents();

        model.addAttribute("incidents", incidents);
        model.addAttribute("pageTitle", "Bancosol | Todas las incidencias");

        return "admin-incidents";
    }

    
}

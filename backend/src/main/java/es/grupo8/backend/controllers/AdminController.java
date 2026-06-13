/**
 * Controlador MVC del panel y vistas del administrador.
 *
 * Autores:
 * - Alejandra Ortiz: 30%
 * - Hugo Herrero González: 30%
 * - Fernando Luis Pinilla Molina: 15%
 * - Alfonso Ramos Rojas: 15%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.dao.GeographicZoneRepository;
import es.grupo8.backend.dao.LocalityRepository;
import es.grupo8.backend.dto.AdminDTO;
import es.grupo8.backend.dto.ChainRequestDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.StoreRequestDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserRoleRequestDto;
import es.grupo8.backend.entity.GeographicZone;
import es.grupo8.backend.entity.Locality;
import es.grupo8.backend.services.AdminService;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignService;
import es.grupo8.backend.services.ChainService;
import es.grupo8.backend.services.PartnerEntityService;
import es.grupo8.backend.services.StoreService;
import es.grupo8.backend.services.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PartnerEntityService partnerEntityService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private ChainService chainService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @Autowired
    private LocalityRepository localityRepository;

    @Autowired
    private GeographicZoneRepository geographicZoneRepository;

    // Listado de campañas de solo lectura, los datos van por el model
    @GetMapping("/campaigns")
    public String campaigns(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        model.addAttribute("campaigns", campaignService.getCampaigns(
                null, PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "startDate"))).getContent());
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

        if (telefono != null && telefono.isBlank()) telefono = null;
        if (domicilio != null && domicilio.isBlank()) domicilio = null;
        if (cp != null && cp.isBlank()) cp = null;

        try {
            authService.register(nombre, email, password, confirmPassword, telefono, domicilio, cp);
            redirectAttributes.addFlashAttribute("success", "Usuario " + nombre + " creado, esperando validación de rol");
            return "redirect:/admin-createusers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el usuario.");
            return "redirect:/admin-createusers";
        }
    }

    @GetMapping("/admin-chains")
    public String adminChains(
            HttpSession session,
            @RequestParam(required = false) Integer crear,
            @RequestParam(required = false) Integer editar,
            Model model) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        model.addAttribute("chains", chainService.findAll());

        if (crear != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("isCreating", true);
        } else if (editar != null) {
            try {
                model.addAttribute("editEntity", chainService.findById(editar));
                model.addAttribute("showForm", true);
            } catch (RuntimeException ignored) {
            }
        }

        return "admin-chains";
    }

    @PostMapping("/admin-chains/guardar")
    public String saveChain(
            HttpSession session,
            @RequestParam(required = false) Integer id,
            @RequestParam String nombre,
            @RequestParam String codigo,
            @RequestParam(required = false, defaultValue = "false") boolean participacion,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        ChainRequestDto dto = new ChainRequestDto();
        dto.setName(nombre);
        dto.setCode(codigo);
        dto.setParticipation(participacion);

        try {
            if (id == null) {
                chainService.create(dto);
                attr.addFlashAttribute("success", "Cadena creada correctamente.");
            } else {
                chainService.update(id, dto);
                attr.addFlashAttribute("success", "Cadena actualizada correctamente.");
            }
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-chains";
    }

    @PostMapping("/admin-chains/eliminar/{id}")
    public String deleteChain(
            HttpSession session,
            @PathVariable Integer id,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        try {
            chainService.delete(id);
            attr.addFlashAttribute("success", "Cadena eliminada.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-chains";
    }

    @GetMapping("/admin-stores")
    public String adminStores(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer localityId,
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(required = false) Integer crear,
            @RequestParam(required = false) Integer editar,
            Model model) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        PaginatedResponse<StoreResponseDto> response =
                storeService.findAll(chainId, localityId, zoneId, page, size);

        model.addAttribute("stores", response.content());
        model.addAttribute("currentPage", response.page());
        model.addAttribute("totalPages", response.totalPages());
        model.addAttribute("currentSize", size);
        model.addAttribute("selectedChainId", chainId);
        model.addAttribute("selectedLocalityId", localityId);
        model.addAttribute("selectedZoneId", zoneId);

        model.addAttribute("chains", chainService.findAll());

        List<GeographicZone> allZones = geographicZoneRepository.findAll();
        model.addAttribute("zones", allZones);

        List<Locality> allLocalities = localityRepository.findAll();
        if (zoneId != null) {
            allLocalities = allLocalities.stream()
                    .filter(l -> l.getIdZone() != null && zoneId.equals(l.getIdZone().getId()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("localities", allLocalities);

        if (crear != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("isCreating", true);
        } else if (editar != null) {
            try {
                model.addAttribute("editEntity", storeService.findById(editar));
                model.addAttribute("showForm", true);
            } catch (RuntimeException ignored) {
            }
        }

        return "admin-stores";
    }

    @PostMapping("/admin-stores/guardar")
    public String saveStore(
            HttpSession session,
            @RequestParam(required = false) Integer id,
            @RequestParam String nombre,
            @RequestParam(required = false) String domicilio,
            @RequestParam(required = false) String cp,
            @RequestParam(required = false) Integer chainId,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        StoreRequestDto dto = new StoreRequestDto();
        dto.setName(nombre);
        dto.setAddress(domicilio);
        dto.setPostalCode(cp);
        dto.setChainId(chainId);

        try {
            if (id == null) {
                storeService.create(dto);
                attr.addFlashAttribute("success", "Tienda creada correctamente.");
            } else {
                storeService.update(id, dto);
                attr.addFlashAttribute("success", "Tienda actualizada correctamente.");
            }
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-stores";
    }

    @PostMapping("/admin-stores/eliminar/{id}")
    public String deleteStore(
            HttpSession session,
            @PathVariable Integer id,
            RedirectAttributes attr) {

        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }

        try {
            storeService.delete(id);
            attr.addFlashAttribute("success", "Tienda eliminada.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-stores";
    }

    @GetMapping("/admin-validate-users")
    public String adminValidateUsers(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role)) {
            return "redirect:/login";
        }
        model.addAttribute("pendingUsers", userService.getPendingUsersOrdered());
        return "admin-validate-users";
    }

    @PostMapping("/admin-validate-users/aprobar/{id}")
    public String approveUser(
            HttpSession session,
            @PathVariable Integer id,
            @RequestParam String role,
            RedirectAttributes attr) {

        String sessionRole = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(sessionRole)) {
            return "redirect:/login";
        }

        try {
            UserRoleRequestDto dto = new UserRoleRequestDto();
            dto.setRole(role);
            userService.assignRole(id, dto);
            attr.addFlashAttribute("success", "Usuario aprobado correctamente.");
        } catch (Exception e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-validate-users";
    }

    @PostMapping("/admin-validate-users/rechazar/{id}")
    public String rejectUser(
            HttpSession session,
            @PathVariable Integer id,
            RedirectAttributes attr) {

        String sessionRole = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(sessionRole)) {
            return "redirect:/login";
        }

        try {
            userService.deleteUser(id);
            attr.addFlashAttribute("success", "Usuario rechazado y eliminado.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-validate-users";
    }

    @GetMapping("/admin-users")
    public String adminUsers(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Model model) {

        String sessionRole = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(sessionRole)) {
            return "redirect:/login";
        }

        PaginatedResponse<UserResponseDto> response =
                userService.getAllUsers(page, size, sort, search, role);

        model.addAttribute("users", response.content());
        model.addAttribute("currentPage", response.page());
        model.addAttribute("totalPages", response.totalPages());
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentRole", role);

        if (sort == null) sort = "id,asc";
        String[] parts = sort.split(",");
        model.addAttribute("sortField", parts[0]);
        model.addAttribute("sortOrder", parts.length > 1 ? parts[1] : "asc");

        return "admin-users";
    }

    @PostMapping("/admin-users/asignar-rol")
    public String assignUserRole(
            HttpSession session,
            @RequestParam Integer userId,
            @RequestParam String role,
            RedirectAttributes attr) {

        String sessionRole = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(sessionRole)) {
            return "redirect:/login";
        }

        try {
            UserRoleRequestDto dto = new UserRoleRequestDto();
            dto.setRole(role);
            userService.assignRole(userId, dto);
            attr.addFlashAttribute("success", "Rol asignado correctamente.");
        } catch (Exception e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-users";
    }

    @PostMapping("/admin-users/eliminar/{id}")
    public String deleteUser(
            HttpSession session,
            @PathVariable Integer id,
            RedirectAttributes attr) {

        String sessionRole = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(sessionRole)) {
            return "redirect:/login";
        }

        try {
            userService.deleteUser(id);
            attr.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-users";
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

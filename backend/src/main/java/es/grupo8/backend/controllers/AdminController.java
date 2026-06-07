/*
* Autores:
*  - Hugo Herrero González: 100%
*
*/

package es.grupo8.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.grupo8.backend.services.AdminService;
import es.grupo8.backend.services.AuthService;
import java.util.List;
import es.grupo8.backend.dto.AdminDTO;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthService authService;

    @GetMapping("/admin-coordinators")
    public String adminCoordinators() {
        return "admin-coordinators";
    }

    @GetMapping("/admin-captains")
    public String adminCaptains() {
        return "admin-captains";
    }

    @GetMapping("/admin-campaigns")
    public String adminCampaigns() {
        return "admin-campaigns";
    }

    @GetMapping("/campaigns")
    public String campaigns() {
        return "campaigns";
    }

    @GetMapping("/admin")
    public String backToMenu() {
        return "admin";
    }

    @GetMapping("/admin-dashboard")
    public String dashboard(@RequestParam(value = "campaignId", required = false) Integer campaignId,
                            HttpSession session,
                            Model model) {

        String role = (String) session.getAttribute("role");
        if(!"ADMINISTRADOR".equals(role)){
            return "redirect:/login";
        }

        model.addAttribute("campaignsList", adminService.getAllCampaigns());
        
        if(campaignId != null) {
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
        if(!"ADMINISTRADOR".equals(role)){
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
	if(!"ADMINISTRADOR".equals(role)){
		return "redirect:/login";
	}

	if (!password.equals(confirmPassword)){
		redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
		return "redirect:/admin-createusers";
	}

	if (password.length() < 6){
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
	} catch (Exception e){
		redirectAttributes.addFlashAttribute("error", "Error al crear el usuario.");
		return "redirect:/admin-createusers";
	}

	}

    @GetMapping("/admin-chains")
    public String adminChains() { return "admin-chains"; }

    @GetMapping("/admin-stores")
    public String adminStores() { return "admin-stores"; }

    @GetMapping("/admin-validate-users")
    public String adminValidateUsers() { return "admin-validate-users"; }

    @GetMapping("/responsible-store")
    public String responsibleStore() { return "responsible-store"; }
}
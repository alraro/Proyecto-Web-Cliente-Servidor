package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.PartnerEntityService;
import es.grupo8.backend.services.UtilsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminPartnerEntitiesController {

    @Autowired
    private PartnerEntityService partnerEntityService;

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

        checkAdmin(session);

        PaginatedResponse<PartnerEntityResponseDto> response =
                partnerEntityService.getAllPartnerEntities(page, size, sort, search);

        model.addAttribute("entities", response.content());
        model.addAttribute("currentPage", response.page());
        model.addAttribute("totalPages", response.totalPages());
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSize", size);

        UtilsService.SortInfo sortInfo = UtilsService.parseSort(sort);
        model.addAttribute("sortField", sortInfo.field());
        model.addAttribute("sortOrder", sortInfo.order());

        if (crear != null) {
            model.addAttribute("showForm", true);
            model.addAttribute("isCreating", true);
        } else if (editar != null) {
            try {
                PartnerEntityResponseDto entity = partnerEntityService.getPartnerEntityById(editar);
                model.addAttribute("editEntity", entity);
                model.addAttribute("showForm", true);
            } catch (RuntimeException e) {
                model.addAttribute("error", "Entidad no encontrada.");
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

        checkAdmin(session);

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

        checkAdmin(session);

        try {
            partnerEntityService.deletePartnerEntity(id);
            attr.addFlashAttribute("success", "Entidad eliminada.");
        } catch (RuntimeException e) {
            attr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin-partner-entities";
    }

    private void checkAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMINISTRADOR".equals(role))
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Acceso denegado");
    }

    @ExceptionHandler(AuthException.class)
    public String handleAuthException() {
        return "redirect:/login";
    }
}

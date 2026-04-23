package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.services.PartnerEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partner-entities")
public class PartnerEntityController {

    @Autowired
    private PartnerEntityService partnerEntityService;

    /**
     * GET /api/partner-entities?page=0&size=20&sort=name,asc&search=banco
     *
     * Query params:
     * - page: página (default 0)
     * - size: elementos por página (default 20, máximo 100)
     * - sort: ordenamiento "field,direction" ej: "name,asc" (opcional)
     * - search: búsqueda por nombre (opcional)
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<PartnerEntityResponseDto>> getPartnerEntities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {

        PaginatedResponse<PartnerEntityResponseDto> response =
                partnerEntityService.getAllPartnerEntities(page, size, sort, search);

        return ResponseEntity.ok(response);
    }
}
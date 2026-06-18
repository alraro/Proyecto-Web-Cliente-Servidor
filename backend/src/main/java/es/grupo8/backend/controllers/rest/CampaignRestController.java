/**
 * Controlador REST para gestionar campañas y sus tipos.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 75%
 * - IA Generativa: 25%
 */
package es.grupo8.backend.controllers.rest;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignRequestDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CampaignRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CampaignService campaignService;

    @GetMapping("/campaign-types")
    public ResponseEntity<?> getCampaignTypes() {
        return ResponseEntity.ok(campaignService.getCampaignTypes());
    }

    @GetMapping("/campaigns")
    public ResponseEntity<?> getCampaigns(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {

        if (size > 50) size = 50;
        else if (size < 1) size = 10;

        Sort sortObj = Sort.by(Sort.Direction.DESC, "startDate");
        String[] parts = sort == null ? new String[0] : sort.split(",");
        if (parts.length == 2) {
            String field = parts[0].trim();
            String dir   = parts[1].trim();
            if (Arrays.asList("startDate", "endDate", "name").contains(field)) {
                if ("asc".equalsIgnoreCase(dir))       sortObj = Sort.by(Sort.Direction.ASC,  field);
                else if ("desc".equalsIgnoreCase(dir)) sortObj = Sort.by(Sort.Direction.DESC, field);
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CampaignDTO> campaignPage = campaignService.getCampaigns(status, pageable);
        long[] counts = campaignService.getCampaignCounts();

        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("page",          campaignPage.getNumber());
        pagination.put("size",          campaignPage.getSize());
        pagination.put("totalElements", campaignPage.getTotalElements());
        pagination.put("totalPages",    campaignPage.getTotalPages());
        pagination.put("isFirst",       campaignPage.isFirst());
        pagination.put("isLast",        campaignPage.isLast());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalActive", counts[0]);
        summary.put("totalPast",   counts[1]);
        summary.put("totalFuture", counts[2]);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content",    campaignPage.getContent());
        response.put("pagination", pagination);
        response.put("summary",    summary);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<?> getCampaignById(@PathVariable Integer id) {
        return campaignService.getCampaignById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Campaign not found")));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CampaignRequestDto request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        CampaignDTO created = campaignService.createCampaign(authService.extractUserIdFromToken(authHeader), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Campaign created successfully", "campaign", created));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<?> updateCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) CampaignRequestDto request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        CampaignDTO updated = campaignService.updateCampaign(authService.extractUserIdFromToken(authHeader), id, request);
        return ResponseEntity.ok(Map.of("message", "Campaign updated successfully", "campaign", updated));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<?> deleteCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        campaignService.deleteCampaign(authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of("message", "Campaign deleted successfully"));
    }

}

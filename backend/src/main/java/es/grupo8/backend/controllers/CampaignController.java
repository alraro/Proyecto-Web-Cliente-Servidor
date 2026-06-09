package es.grupo8.backend.controllers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Campaigns", description = "Campaign management — Admin only for write operations")
@AllArgsConstructor
public class CampaignController {

    private final AuthService authService;
    private final UserService userService;
    private final CampaignService campaignService;

    @GetMapping("/campaign-types")
    @Operation(summary = "List all campaign types")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Campaign types listed successfully") })
    public ResponseEntity<?> getCampaignTypes() {
        return ResponseEntity.ok(campaignService.getCampaignTypes());
    }

    @GetMapping("/campaigns")
    @Operation(summary = "List campaigns with optional status filter and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaigns listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<?> getCampaigns(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {

        if (size > 50) size = 50;
        else if (size < 1)  size = 10;

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
    @Operation(summary = "Get a single campaign by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign found"),
            @ApiResponse(responseCode = "404", description = "Campaign not found")
    })
    public ResponseEntity<?> getCampaignById(@PathVariable Integer id) {
        return campaignService.getCampaignById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Campaign not found")));
    }

    @PostMapping("/campaigns")
    @Operation(summary = "Create a new campaign — Admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campaign created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
            @ApiResponse(responseCode = "404", description = "Campaign type not found"),
            @ApiResponse(responseCode = "409", description = "Campaign name conflict")
    })
    public ResponseEntity<?> createCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CampaignRequestDto request) {

        if (!userService.isAdminFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to administrators"));
        }

        CampaignDTO created = campaignService.createCampaign(authService.extractUserIdFromToken(authHeader), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Campaign created successfully", "campaign", created));
    }

    @PutMapping("/campaigns/{id}")
    @Operation(summary = "Update an existing campaign — Admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
            @ApiResponse(responseCode = "404", description = "Campaign or campaign type not found"),
            @ApiResponse(responseCode = "409", description = "Campaign name conflict")
    })
    public ResponseEntity<?> updateCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) CampaignRequestDto request) {

        if (!userService.isAdminFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to administrators"));
        }

        CampaignDTO updated = campaignService.updateCampaign(authService.extractUserIdFromToken(authHeader), id, request);
        return ResponseEntity.ok(Map.of("message", "Campaign updated successfully", "campaign", updated));
    }

    @DeleteMapping("/campaigns/{id}")
    @Operation(summary = "Delete a campaign and its assignments — Admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
            @ApiResponse(responseCode = "404", description = "Campaign not found")
    })
    public ResponseEntity<?> deleteCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to administrators"));
        }

        campaignService.deleteCampaign(authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of("message", "Campaign deleted successfully"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
}

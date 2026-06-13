/**
 * Controlador REST para gestionar las tiendas de una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 70%
 * - Alejandra Ortiz: 5%
 * - Alfonso Ramos Rojas: 5%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignStoreService;
import es.grupo8.backend.services.UserService;
import es.grupo8.backend.util.BodyParams;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class CampaignStoreRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CampaignStoreService campaignStoreService;

    @GetMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> getCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        return ResponseEntity.ok(campaignStoreService.getCampaignStores(
                authService.extractUserIdFromToken(authHeader), campaignId));
    }

    @PostMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> assignStoreToCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");

        Integer storeId = BodyParams.toInteger(request == null ? null : request.get("storeId"));
        if (storeId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeId is required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignStoreService.assignStoreToCampaign(
                        authService.extractUserIdFromToken(authHeader), campaignId, storeId));
    }

    @DeleteMapping("/api/campaigns/{campaignId}/stores/{storeId}")
    public ResponseEntity<?> removeStoreFromCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer storeId) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        campaignStoreService.removeStoreFromCampaign(
                authService.extractUserIdFromToken(authHeader), campaignId, storeId);
        return ResponseEntity.ok(Map.of("message", "Tienda desasignada correctamente"));
    }

    @PutMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> replaceCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");

        List<Integer> storeIds = BodyParams.toIntegerList(request == null ? null : request.get("storeIds"));
        if (storeIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeIds list is required"));
        }
        return ResponseEntity.ok(campaignStoreService.replaceCampaignStores(
                authService.extractUserIdFromToken(authHeader), campaignId, storeIds));
    }
}

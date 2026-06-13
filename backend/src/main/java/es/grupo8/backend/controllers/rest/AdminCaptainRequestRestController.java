/**
 * Controlador REST para gestionar solicitudes de alta de capitanes.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.services.AdminCaptainRequestService;
import es.grupo8.backend.services.AdminService;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminCaptainRequestRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final AdminCaptainRequestService adminCaptainRequestService;
    private final AdminService adminService;

    @GetMapping("/captain-requests")
    public ResponseEntity<?> getCaptainRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", defaultValue = "PENDIENTE") String status) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        List<Map<String, Object>> result = adminCaptainRequestService.getRequests(status).stream()
                .map(this::requestToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/captain-requests/{id}/approve")
    public ResponseEntity<?> approveCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        Integer newUserId = adminCaptainRequestService.approveRequest(
                authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of(
                "message", "Capitán aprobado y creado correctamente.",
                "userId",  newUserId));
    }

    @PostMapping("/captain-requests/{id}/reject")
    public ResponseEntity<?> rejectCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden("administrators");
        adminCaptainRequestService.rejectRequest(authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of("message", "Solicitud rechazada."));
    }

    private Map<String, Object> requestToMap(CaptainRequest r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              r.getId());
        m.put("name",            r.getName());
        m.put("email",           r.getEmail());
        m.put("status",          r.getStatus());
        m.put("createdAt",       r.getCreatedAt()  != null ? r.getCreatedAt().toString()  : null);
        m.put("resolvedAt",      r.getResolvedAt() != null ? r.getResolvedAt().toString() : null);
        m.put("campaignId",      r.getIdCampaign()    != null ? r.getIdCampaign().getId()       : null);
        m.put("campaignName",    r.getIdCampaign()    != null ? r.getIdCampaign().getName()     : null);
        m.put("coordinatorName", r.getIdCoordinator() != null ? r.getIdCoordinator().getName()  : null);
        return m;
    }


    @GetMapping("/incidents")
    public ResponseEntity<List<Map<String, Object>>> getIncidents() {
        List<Map<String, Object>> incidents = adminService.getAllIncidents("desc");

        return ResponseEntity.ok(incidents);
    }

    @DeleteMapping("/incidents/{id}")
    public ResponseEntity<?> deleteIncident(@PathVariable Integer id) {
        try {
            adminService.deleteIncident(id);
            return ResponseEntity.ok("Incidencia eliminada");
        } catch(Exception e){
            return ResponseEntity.badRequest().body("Error al eliminar");
        }
    }


}

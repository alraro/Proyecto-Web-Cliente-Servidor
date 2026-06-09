package es.grupo8.backend.controllers.rest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

/**
 * REST controller for admin management of captain registration requests.
 * Admins can list, approve and reject pending captain requests.
 */
@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminCaptainRequestRestController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final AuthService               authService;
    private final UserService               userService;
    private final CaptainRequestRepository  captainRequestRepository;
    private final UserRepository            userRepository;
    private final CaptainRepository         captainRepository;

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Returns captain registration requests filtered by status.
     *
     * @param authHeader JWT Authorization header
     * @param status     status filter (default PENDIENTE)
     * @return list of request maps
     */
    @GetMapping("/captain-requests")
    public ResponseEntity<?> getCaptainRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", defaultValue = "PENDIENTE") String status) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        List<CaptainRequest> requests = captainRequestRepository.findByStatus(status.toUpperCase());
        List<Map<String, Object>> result = requests.stream()
                .map(this::requestToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Approves a captain registration request: creates the user, assigns the captain role
     * and marks the request as APROBADA.
     *
     * @param authHeader JWT Authorization header
     * @param id         request identifier
     * @return new user id and success message
     */
    @PostMapping("/captain-requests/{id}/approve")
    public ResponseEntity<?> approveCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        Integer adminUserId = authService.extractUserIdFromToken(authHeader);

        CaptainRequest req = captainRequestRepository.findById(id).orElse(null);
        if (req == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Solicitud no encontrada"));
        }
        if (!"PENDIENTE".equals(req.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Esta solicitud ya fue procesada"));
        }

        UserEntity newUser = new UserEntity();
        newUser.setName(req.getName());
        newUser.setEmail(req.getEmail());
        newUser.setPassword(req.getPasswordHash());
        UserEntity savedUser = userRepository.save(newUser);

        CaptainId captainId = new CaptainId();
        captainId.setIdUser(savedUser.getIdUser());
        captainId.setIdCampaign(req.getIdCampaign().getId());

        Captain captain = new Captain();
        captain.setId(captainId);
        captain.setIdUser(savedUser);
        captain.setIdCampaign(req.getIdCampaign());
        captainRepository.save(captain);

        req.setStatus("APROBADA");
        req.setResolvedAt(Instant.now());
        captainRequestRepository.save(req);

        auditLog.info("ACTION=APPROVE_CAPTAIN_REQUEST adminUserId={} timestamp={} requestId={} newUserId={}",
                adminUserId, Instant.now(), id, savedUser.getIdUser());

        return ResponseEntity.ok(Map.of(
                "message", "Capitán aprobado y creado correctamente.",
                "userId",  savedUser.getIdUser()
        ));
    }

    /**
     * Rejects a captain registration request and marks it as RECHAZADA.
     *
     * @param authHeader JWT Authorization header
     * @param id         request identifier
     * @return success message
     */
    @PostMapping("/captain-requests/{id}/reject")
    public ResponseEntity<?> rejectCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        Integer adminUserId = authService.extractUserIdFromToken(authHeader);

        CaptainRequest req = captainRequestRepository.findById(id).orElse(null);
        if (req == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Solicitud no encontrada"));
        }
        if (!"PENDIENTE".equals(req.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Esta solicitud ya fue procesada"));
        }

        req.setStatus("RECHAZADA");
        req.setResolvedAt(Instant.now());
        captainRequestRepository.save(req);

        auditLog.info("ACTION=REJECT_CAPTAIN_REQUEST adminUserId={} timestamp={} requestId={}",
                adminUserId, Instant.now(), id);

        return ResponseEntity.ok(Map.of("message", "Solicitud rechazada."));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access restricted to administrators"));
    }

    /**
     * Converts a {@link CaptainRequest} entity to a plain map for the API response.
     *
     * @param r captain request entity
     * @return map of response fields
     */
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
}

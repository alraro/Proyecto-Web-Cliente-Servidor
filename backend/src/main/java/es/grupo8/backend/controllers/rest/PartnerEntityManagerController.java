package es.grupo8.backend.controllers.rest;

import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityManagerAssignRequestDto;
import es.grupo8.backend.dto.PartnerEntityManagerResponseDto;
import es.grupo8.backend.dto.PartnerEntityManagerUpdateRequestDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.PartnerEntityManagerService;
import es.grupo8.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partner-entity-managers")
public class PartnerEntityManagerController {

    @Autowired
    private PartnerEntityManagerService partnerEntityManagerService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    private Integer checkAuth(String auth) {
        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
        return userId;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<PartnerEntityManagerResponseDto>> getPartnerEntityManagers(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {

        checkAuth(auth);
        PaginatedResponse<PartnerEntityManagerResponseDto> response =
                partnerEntityManagerService.getAllPartnerEntityManagers(page, size, sort, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PartnerEntityManagerResponseDto> getPartnerEntityManagerByUserId(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer userId) {

        checkAuth(auth);
        PartnerEntityManagerResponseDto response = partnerEntityManagerService.getPartnerEntityManagerByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<PartnerEntityManagerResponseDto> promoteUserToPartnerEntityManager(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer userId,
            @RequestBody(required = false) PartnerEntityManagerAssignRequestDto request) {

        checkAuth(auth);
        PartnerEntityManagerResponseDto response =
                partnerEntityManagerService.promoteUserToPartnerEntityManager(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<PartnerEntityManagerResponseDto> updatePartnerEntityManager(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer userId,
            @RequestBody PartnerEntityManagerUpdateRequestDto request) {

        checkAuth(auth);
        PartnerEntityManagerResponseDto response =
                partnerEntityManagerService.updatePartnerEntityManager(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removePartnerEntityManagerRole(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer userId) {

        checkAuth(auth);
        partnerEntityManagerService.removePartnerEntityManagerRole(userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }
}

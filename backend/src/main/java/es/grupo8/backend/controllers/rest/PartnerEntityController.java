package es.grupo8.backend.controllers.rest;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.PartnerEntityService;
import es.grupo8.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partner-entities")
public class PartnerEntityController {

    @Autowired
    private PartnerEntityService partnerEntityService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    private void checkAdmin(String auth) {
        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    private void checkAdminOrEntityManager(String auth, Integer entityId) {
        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId) && !userService.isManagerOfEntity(userId, entityId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<PartnerEntityResponseDto>> getPartnerEntities(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {

        checkAdmin(auth);
        PaginatedResponse<PartnerEntityResponseDto> response =
                partnerEntityService.getAllPartnerEntities(page, size, sort, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerEntityResponseDto> getPartnerEntityById(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id) {

        checkAdminOrEntityManager(auth, id);
        PartnerEntityResponseDto response = partnerEntityService.getPartnerEntityById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PartnerEntityResponseDto> createPartnerEntity(
            @RequestHeader("Authorization") String auth,
            @RequestBody PartnerEntityRequestDto request) {

        checkAdmin(auth);
        PartnerEntityResponseDto response = partnerEntityService.createPartnerEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerEntityResponseDto> updatePartnerEntity(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id,
            @RequestBody PartnerEntityRequestDto request) {

        checkAdminOrEntityManager(auth, id);
        PartnerEntityResponseDto response = partnerEntityService.updatePartnerEntity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartnerEntity(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer id) {

        checkAdmin(auth);
        partnerEntityService.deletePartnerEntity(id);
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

/**
 * Autores:
 * - Alfonso Ramos Rojas: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers.rest;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.PartnerEntityService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner-entities")
@AllArgsConstructor
public class PartnerEntityController extends BaseRestController {

    private final PartnerEntityService partnerEntityService;
    private final AuthService authService;
    private final UserService userService;

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
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {

        authService.checkAdmin(auth);
        PaginatedResponse<PartnerEntityResponseDto> response =
                partnerEntityService.getAllPartnerEntities(page, size, sort, search);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerEntityResponseDto> getPartnerEntityById(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer id) {

        checkAdminOrEntityManager(auth, id);
        PartnerEntityResponseDto response = partnerEntityService.getPartnerEntityById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PartnerEntityResponseDto> createPartnerEntity(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody PartnerEntityRequestDto request) {

        authService.checkAdmin(auth);
        PartnerEntityResponseDto response = partnerEntityService.createPartnerEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerEntityResponseDto> updatePartnerEntity(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer id,
            @RequestBody PartnerEntityRequestDto request) {

        checkAdminOrEntityManager(auth, id);
        PartnerEntityResponseDto response = partnerEntityService.updatePartnerEntity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartnerEntity(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer id) {

        authService.checkAdmin(auth);
        partnerEntityService.deletePartnerEntity(id);
        return ResponseEntity.noContent().build();
    }
}

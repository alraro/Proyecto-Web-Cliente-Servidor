/**
 * Controlador REST para gestionar tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.StoreRequestDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.StoreService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@AllArgsConstructor
public class StoreController extends BaseRestController {

    private final StoreService storeService;
    private final AuthService authService;
    private final UserService userService;

    private void checkAdminOrCoordinator(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId) && !userService.isCoordinator(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<StoreResponseDto>> getStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer localityId,
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        checkAdminOrCoordinator(authHeader);

        PaginatedResponse<StoreResponseDto> result = storeService.findAll(search, chainId, localityId, zoneId, page, size, sort);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDto> getStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        authService.checkAdmin(authHeader);
        return ResponseEntity.ok(storeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) StoreRequestDto req) {

        authService.checkAdmin(authHeader);
        StoreResponseDto created = storeService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponseDto> updateStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) StoreRequestDto req) {

        authService.checkAdmin(authHeader);
        StoreResponseDto updated = storeService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        authService.checkAdmin(authHeader);
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

/**
 * Controlador REST para la vista de responsable de tienda.
 *
 * Autores:
 * - Alejandra Ortiz: 100%
 */
package es.grupo8.backend.controllers.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.ResponsibleStoreService;
import lombok.AllArgsConstructor;

/**
 * REST controller para GET /api/stores/{id}/detail.
 * Refactorizado: extrae el userId del JWT y delega en ResponsibleStoreService.
 * La lógica de verificación de acceso y construcción de respuesta está en el service.
 */
@RestController
@RequestMapping("/api/stores")
@AllArgsConstructor
public class ResponsibleStoreController extends BaseRestController {

    private final ResponsibleStoreService responsibleStoreService;
    private final AuthService authService;

    private void checkAuth(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getStoreDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAuth(authHeader);
        Integer userId = authService.extractUserIdFromToken(authHeader);
        Map<String, Object> detail = responsibleStoreService.getStoreDetail(id, userId);
        return ResponseEntity.ok(detail);
    }
}

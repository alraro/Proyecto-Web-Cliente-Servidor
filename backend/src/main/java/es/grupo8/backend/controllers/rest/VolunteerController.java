/**
 * Autores:
 * - Alfonso Ramos Rojas: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers.rest;

import es.grupo8.backend.dto.VoluntarioRequestDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voluntarios")
public class VolunteerController extends BaseRestController {

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private AuthService authService;

    private void checkAuth(String auth, Integer entidadId) {
        Integer userId = authService.extractUserIdFromToken(auth);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!volunteerService.canAccessPartnerEntity(userId, entidadId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<List<VoluntarioResponseDto>> listarVoluntarios(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam Integer entidadId) {

        checkAuth(auth, entidadId);
        List<VoluntarioResponseDto> voluntarios = volunteerService.getVolunteersByEntity(entidadId);
        return ResponseEntity.ok(voluntarios);
    }

    @PostMapping
    public ResponseEntity<VoluntarioResponseDto> crearVoluntario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody VoluntarioRequestDto request,
            @RequestParam Integer entidadId) {

        checkAuth(auth, entidadId);
        VoluntarioResponseDto voluntario = volunteerService.createVolunteer(request, entidadId);
        return ResponseEntity.status(HttpStatus.CREATED).body(voluntario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoluntarioResponseDto> editarVoluntario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody VoluntarioRequestDto request,
            @RequestParam Integer entidadId,
            @PathVariable Integer id) {

        checkAuth(auth, entidadId);
        VoluntarioResponseDto voluntario = volunteerService.updateVolunteer(id, request, entidadId);
        return ResponseEntity.ok(voluntario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVoluntario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam Integer entidadId,
            @PathVariable Integer id) {

        checkAuth(auth, entidadId);
        volunteerService.deleteVolunteer(id, entidadId);
        return ResponseEntity.noContent().build();
    }
}

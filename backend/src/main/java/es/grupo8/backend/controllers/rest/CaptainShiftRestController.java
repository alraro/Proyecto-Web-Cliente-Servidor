/**
 * Controlador REST de consulta de turnos del equipo del capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CaptainShiftService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/shifts")
@AllArgsConstructor
public class CaptainShiftRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CaptainShiftService captainShiftService;

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeamShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainShiftService.getMyTeamShifts(
                authService.extractUserIdFromToken(authHeader), campaignId));
    }

}
